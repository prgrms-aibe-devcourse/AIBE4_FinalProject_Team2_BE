package com.aibe.team2.domain.file.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.s3.endpoint:#{null}}")
    private String s3Endpoint;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.credentials.access-key:#{null}}")
    private String accessKey;

    @Value("${aws.credentials.secret-key:#{null}}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region));

        applyEndpointAndCredentials(builder);

        if (isLocalstackEndpoint()) {
            builder.serviceConfiguration(
                    S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build()
            );
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(region));

        applyEndpointAndCredentials(builder);

        // LocalStack에서 presign도 path-style이 필요하면 켜기
        if (isLocalstackEndpoint()) {
            builder.serviceConfiguration(
                    S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .build()
            );
        }

        return builder.build();
    }

    private boolean isLocalstackEndpoint() {
        return s3Endpoint != null && !s3Endpoint.isBlank();
    }

    private boolean hasStaticCredentials() {
        return accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank();
    }

    private void applyEndpointAndCredentials(S3ClientBuilder builder) {
        if (isLocalstackEndpoint()) {
            builder.endpointOverride(URI.create(s3Endpoint));
        }

        if (hasStaticCredentials()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    )
            );
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
    }

    private void applyEndpointAndCredentials(S3Presigner.Builder builder) {
        if (isLocalstackEndpoint()) {
            builder.endpointOverride(URI.create(s3Endpoint));
        }

        if (hasStaticCredentials()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    )
            );
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
    }
}