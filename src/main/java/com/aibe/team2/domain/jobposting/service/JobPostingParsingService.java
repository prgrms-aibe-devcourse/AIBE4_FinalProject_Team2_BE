package com.aibe.team2.domain.jobposting.service;

import com.aibe.team2.domain.jobposting.dto.JobPostingParseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostingParsingService {

    // private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public JobPostingParseResponse autoFillFromUrl(String url) {
        String rawText = crawlWebPage(url);

        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("크롤링할 수 없는 URL이거나 내용이 비어있습니다.");
        }

        // 프롬프트 수정: 스킬 대신 면접 질문(expectedQuestions) 생성 요청
        String prompt = """
            다음 채용 공고 텍스트를 분석해서 아래 JSON 형식에 맞게 데이터를 추출하고, 공고 내용을 바탕으로 지원자에게 물어볼 만한 핵심 예상 면접 질문 5가지를 생성해줘.
            반드시 순수 JSON 포맷으로만 대답해. (Markdown 텍스트 블록 제외)
            
            {
              "companyName": "기업명",
              "jobTitle": "직무명",
              "jobDescription": "공고 전체 내용 요약 또는 원본",
              "mainTasks": "주요업무 내용",
              "qualifications": "자격요건 내용",
              "preferred": "우대사항 내용",
              "benefits": "복리후생 내용",
              "expectedQuestions": [
                 "공고의 주요 업무와 관련된 예상 질문 1",
                 "공고의 자격 요건을 검증하는 예상 질문 2",
                 "예상 질문 3",
                 "예상 질문 4",
                 "예상 질문 5"
              ]
            }
            
            [채용 공고 본문]
            """ + rawText;

        // 임시 주석 처리 (실제 연동 시 활성화)
        // String aiJsonResponse = geminiService.generateText(prompt);
        // return objectMapper.readValue(aiJsonResponse, JobPostingParseResponse.class);

        return null;
    }

    private String crawlWebPage(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();
            return doc.body().text();
        } catch (IOException e) {
            log.error("웹 페이지 크롤링 실패: {}", url, e);
            return null;
        }
    }
}