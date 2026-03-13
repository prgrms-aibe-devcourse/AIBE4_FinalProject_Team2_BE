package com.aibe.team2.domain.jobposting.service;

import com.aibe.team2.domain.jobposting.dto.JobPostingParseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostingParsingService {

    // private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    // [리뷰 반영] 크롤링 전담 서비스 주입
    private final JobPostingCrawlerService jobPostingCrawlerService;

    public JobPostingParseResponse autoFillFromUrl(String url) {
        // [리뷰 반영] 중복 코드를 제거하고 크롤링 전담 서비스의 메서드를 호출
        String rawText = jobPostingCrawlerService.crawlFullText(url);

        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("크롤링할 수 없는 URL이거나 내용이 비어있습니다.");
        }

        // 프롬프트 수정: 스킬 대신 면접 질문(expectedQuestions) 생성 요청
        String prompt = """
            다음 채용 공고 텍스트를 분석해서 아래 JSON 형식에 맞게 데이터를 추출해줘.
            공고 내용을 바탕으로 지원자에게 물어볼 만한 핵심 예상 면접 질문 5가지도 함께 생성해.
            반드시 순수 JSON 포맷으로만 대답해. (Markdown 텍스트 블록 제외)
            
            {
              "companyName": "기업명",
              "jobTitle": "직무명",
              "jobDescription": "공고 전체 내용 요약 또는 원본",
              "mainTasks": "주요업무 내용",
              "qualifications": "자격요건 내용",
              "preferred": "우대사항 내용",
              "benefits": "복리후생 내용",
              "requiredSkills": ["Java", "Spring Boot", "MySQL" 등 기술 스택 배열], 
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

        return new JobPostingParseResponse(null, null, null, null, null, null, null,  null, java.util.Collections.emptyList());
    }


}