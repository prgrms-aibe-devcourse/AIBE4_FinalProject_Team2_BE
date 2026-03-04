package com.aibe.team2.domain.file.service;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class S3ImageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    // 허용하는 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    // 최대 파일 크기 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // 버킷 생성
    @PostConstruct
    public void initBucket() {
        try {
            // 1. 버킷이 존재하는지 찔러보기
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            // 2. 404(Not Found) 에러가 나면 버킷이 없다는 뜻이므로 새로 생성
            if (e.statusCode() == 404) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                log.info("✅ S3ImageService: 테스트용 버킷 [{}] 자동 생성 완료!", bucket);
            }
        } catch (Exception e) {
            log.error("버킷 확인 중 에러 발생: {}", e.getMessage(), e);
        }
    }

    // MultipartFile을 S3에 업로드하고 URL 반환
    public String uploadProfileImage(MultipartFile file, Long memberId) {
        validateImageFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);

        // 경로 : profiles/{memberId}/{UUID}.{확장자}
        String s3Key = "profiles/" + memberId + "/" + UUID.randomUUID() + "." + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 업로드된 파일의 S3 접근 URL 생성(S3Client 활용)
            return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(s3Key)).toExternalForm();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // S3에서 기존 이미지 삭제
    public void deleteImage(String fileUrl) {
        if(fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        // URL에서 S3 Key 부분만 추출
        String fileKey = extractKeyFromUrl(fileUrl);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            log.error("S3 파일 삭제 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    // 파일 확장자 및 크기 검사
    private void validateImageFile(MultipartFile file) {
        if(file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        if(file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String extension = getExtension(file.getOriginalFilename()).toLowerCase();
        if(!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_INVALID);
        }
    }

    private String getExtension(String filename) {
        if(filename == null || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.FILE_EXTENSION_INVALID);
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String extractKeyFromUrl(String fileUrl) {
        try {
            java.net.URL url = new java.net.URL(fileUrl);
            String path = url.getPath(); // 예: "/profiles/1/uuid.jpg" 또는 "/resumes/uuid.pdf"

            return path.startsWith("/") ? path.substring(1) : path;
        } catch (java.net.MalformedURLException e) {
            throw new BusinessException(ErrorCode.COMMON_400); // 잘못된 URL 형식 예외 처리
        }
    }
}
