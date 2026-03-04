package com.aibe.team2.domain.resume.service;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Component
public class ResumeParsingEngine {

    private final Tika tika;

    public ResumeParsingEngine() {
        this.tika = new Tika();
    }

    // 클라이언트가 업로드한 MultipartFile에서 텍스트를 추출합니다

    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY); // 프로젝트에 맞는 에러 코드 사용
        }

        try (InputStream inputStream = file.getInputStream()) {
            // Tika가 파일 자동 감지하여 텍스트를 추출
            String extractedText = tika.parseToString(inputStream);

            // 앞뒤 공백 제거 후 반환
            return extractedText != null ? extractedText.trim() : "";

        } catch (Exception e) {
            log.error("[Resume Parsing Error] 파일 명: {}, 오류: {}", file.getOriginalFilename(), e.getMessage());
            throw new BusinessException(ErrorCode.RESUME_PARSING_ERROR);
        }
    }
}