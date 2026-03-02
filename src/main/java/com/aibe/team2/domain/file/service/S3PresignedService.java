package com.aibe.team2.domain.file.service;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@Service
public class S3PresignedService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.allowed-content-types}")
    private String allowedContentTypes; // "application/pdf,image/png,image/jpeg"

    @Value("${aws.s3.presign-expiration-minutes:10}")
    private long presignExpirationMinutes;

    @Value("${aws.s3.endpoint:#{null}}")
    private String endpoint;

    public S3PresignedService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * 앱 시작 시 버킷 존재를 보장.
     */
    @Component
    @Profile("docker")
    static class BucketInitializer {

        private final S3PresignedService service;

        BucketInitializer(S3PresignedService service) {
            this.service = service;
        }

        @PostConstruct
        void init() {
            if (service.isEndpointOverrideEnabled()) {
                service.ensureBucketExists();
            }
        }
    }

    boolean isEndpointOverrideEnabled() {
        return endpoint != null && !endpoint.isBlank();
    }

    // package-private
    void ensureBucketExists() {
        try {
            s3Client.headBucket(
                    HeadBucketRequest.builder()
                            .bucket(bucket)
                            .build()
            );
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                s3Client.createBucket(
                        CreateBucketRequest.builder()
                                .bucket(bucket)
                                .build()
                );
            } else {
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        } catch (SdkException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 업로드용 Presigned PUT URL 생성
     * - contentType 검증
     * - 파일명 sanitize
     * - key를 회원별 prefix로 강제하여 IDOR 완화
     */
    public PresignedUrlResponse generatePutPresignedUrl(Long ownerMemberId, String originalFileName, String contentType) {
        if (ownerMemberId == null || ownerMemberId <= 0) {
            throw new BusinessException(ErrorCode.COMMON_400);
        }

        validateContentType(contentType);

        String sanitizedFileName = sanitizeFileName(originalFileName);

        String key = buildMemberScopedKey(ownerMemberId, sanitizedFileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignExpirationMinutes))
                .putObjectRequest(putObjectRequest)
                .build();

        String url = s3Presigner.presignPutObject(presignRequest).url().toString();
        return new PresignedUrlResponse(url, key);
    }

    public HeadObjectResponse headObject(String key) {
        try {
            return s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
        } catch (NoSuchKeyException e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
            }
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (SdkException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public PresignedUrlResponse generateGetPresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignExpirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        String url = s3Presigner.presignGetObject(presignRequest).url().toString();
        return new PresignedUrlResponse(url, key);
    }

    private void validateContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new BusinessException(ErrorCode.COMMON_400);
        }

        if (allowedContentTypes == null || allowedContentTypes.isBlank()) {
            throw new BusinessException(ErrorCode.SYS_INTERNAL_ERROR);
        }

        boolean ok = Arrays.stream(allowedContentTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .anyMatch(allowed -> allowed.equalsIgnoreCase(contentType));

        if (!ok) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_INVALID);
        }
    }

    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        String base = originalFileName
                .replace("\\", "_")
                .replace("/", "_");

        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String buildMemberScopedKey(Long ownerMemberId, String sanitizedFileName) {
        return "uploads/members/" + ownerMemberId + "/" + UUID.randomUUID() + "-" + sanitizedFileName;
    }

    public record PresignedUrlResponse(String url, String key) {}
}