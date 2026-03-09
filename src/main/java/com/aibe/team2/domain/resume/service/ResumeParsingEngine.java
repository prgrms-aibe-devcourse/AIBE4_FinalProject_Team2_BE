package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.dto.ResumeParsedItem;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ResumeParsingEngine {

    private final Tika tika;

    public ResumeParsingEngine() {
        this.tika = new Tika();
    }

    public List<ResumeParsedItem> extractAndSplitText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        try (InputStream inputStream = file.getInputStream()) {
            String extractedText = tika.parseToString(inputStream);
            if (extractedText == null || extractedText.trim().isEmpty()) {
                return new ArrayList<>();
            }

            // 정규표현식을 통해 소제목과 내용 분리
            return splitByRegex(extractedText.trim());

        } catch (Exception e) {
            log.error("[Resume Parsing Error] 파일 명: {}, 오류: {}", file.getOriginalFilename(), e.getMessage());
            throw new BusinessException(ErrorCode.RESUME_PARSING_ERROR);
        }
    }

    private List<ResumeParsedItem> splitByRegex(String text) {
        List<ResumeParsedItem> result = new ArrayList<>();

        // 정규식 패턴: 줄의 시작이 숫자+점(1. ), 대괄호([ ]), 혹은 특정 특수문자(■, ◆)인 경우를 소제목으로 간주

        String regex = "(?m)^(\\d+\\.|\\s*\\[.*?\\]|■|◆).*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        String currentSubtitle = "기본 정보"; // 맨 처음 소제목이 나오기 전의 텍스트를 담을 기본 제목

        while (matcher.find()) {
            // 이전 소제목에 딸린 내용을 추출
            String content = text.substring(lastEnd, matcher.start()).trim();
            if (!content.isEmpty() || !result.isEmpty()) {
                result.add(new ResumeParsedItem(currentSubtitle, content));
            }

            // 새로운 소제목 갱신 (예: "1.", "지원동기"를 합치거나, 그룹 2인 "지원동기"만 가져옴)
            currentSubtitle = matcher.group(0).trim();
            lastEnd = matcher.end();
        }

        // 마지막 소제목에 딸린 내용 추가
        if (lastEnd < text.length()) {
            String lastContent = text.substring(lastEnd).trim();
            result.add(new ResumeParsedItem(currentSubtitle, lastContent));
        }

        return result;
    }
}