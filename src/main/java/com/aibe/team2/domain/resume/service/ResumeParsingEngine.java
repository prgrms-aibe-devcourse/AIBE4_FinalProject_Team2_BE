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

            // 1. HWPX 메타데이터 쓰레기값 및 미리보기 영역 청소
            String cleanedText = cleanHwpxGarbage(extractedText);

            // 🔥 결정적 해결책: 연속된 공백(3개 이상)을 강제로 줄바꿈(\n)으로 변환
            // Tika가 HWPX 본문을 읽을 때 줄바꿈을 무시하고 스페이스바 여러 개로 뭉뚱그리는 현상 완벽 해결!
            // \u00A0(Non-breaking space)와 \u3000(Ideographic space) 등 특수 공백까지 모두 잡아냅니다.
            cleanedText = cleanedText.replaceAll("[ \\t\\u00A0\\u3000]{3,}", "\n");

            // 2. 줄 단위 분석을 통해 소제목과 내용 분리
            return splitByLineAnalysis(cleanedText);

        } catch (Exception e) {
            log.error("[Resume Parsing Error] 파일 명: {}, 오류: {}", file.getOriginalFilename(), e.getMessage());
            throw new BusinessException(ErrorCode.RESUME_PARSING_ERROR);
        }
    }

    private String cleanHwpxGarbage(String text) {
        // 🔥 핵심 수정 1: Preview 텍스트는 내용이 잘리므로, 잘리기 전 원본 전체 내용(Contents/section 등)만 사용합니다.
        int previewIdx = text.indexOf("Preview/PrvText.txt");
        if (previewIdx != -1) {
            text = text.substring(0, previewIdx);
        }

        String[] lines = text.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // 시스템 메타데이터 파일명 무시
            if (trimmed.equals("mimetype") ||
                    trimmed.equals("application/hwp+zip") ||
                    trimmed.startsWith("version.xml") ||
                    trimmed.startsWith("Contents/") ||
                    trimmed.startsWith("META-INF/") ||
                    trimmed.matches("^\\^\\d+.*")) {
                continue;
            }
            sb.append(line).append("\n");
        }

        return sb.toString().trim();
    }

    private List<ResumeParsedItem> splitByLineAnalysis(String text) {
        List<ResumeParsedItem> result = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");

        int questionCount = 1;
        String currentSubtitle = "기본 정보";
        StringBuilder currentContent = new StringBuilder();

        String prefixRegex = "^\\s*(Q\\d*\\.?|\\d+[\\.\\)]|\\[.*?\\]|■|◆|▶|●|제\\s*\\d+\\s*항).*";
        String suffixRegex = ".*(기술\\s*바랍니다\\.?|주시기\\s*바랍니다\\.?|서술하시오\\.?|입력\\s*가능\\)?|기재해\\s*주시기\\s*바랍니다\\.?|글자\\s*수|자\\s*이내).*";

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;

            boolean isQuestion = (trimmedLine.matches(prefixRegex) || trimmedLine.matches(suffixRegex))
                    && trimmedLine.length() < 150;

            if (isQuestion) {
                // 🔥 핵심 수정 2: 내용(답변)이 비어있더라도, 맨 처음 임시값인 '기본 정보'가 아니라면 객체로 저장합니다. (빈 답변 문항 증발 방지)
                if (currentContent.length() > 0 || !currentSubtitle.equals("기본 정보")) {
                    result.add(new ResumeParsedItem(currentSubtitle, currentContent.toString().trim()));
                    currentContent.setLength(0); // 내용 초기화
                }

                currentSubtitle = trimmedLine;

                if (!currentSubtitle.matches("^\\s*\\d+.*")) {
                    currentSubtitle = questionCount + ". " + currentSubtitle;
                }
                questionCount++;
            } else {
                if (currentContent.length() > 0) {
                    currentContent.append("\n");
                }
                currentContent.append(trimmedLine);
            }
        }

        // 반복문 종료 후 마지막 항목 저장
        if (currentContent.length() > 0 || !currentSubtitle.equals("기본 정보")) {
            result.add(new ResumeParsedItem(currentSubtitle, currentContent.toString().trim()));
        }

        return result;
    }
}