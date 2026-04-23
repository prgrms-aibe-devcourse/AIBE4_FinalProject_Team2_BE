package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.InterviewRequestDto;
import com.aibe.team2.domain.interview.enums.ExperienceLevel;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.JobRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {
    private final WebClient webClient;
    private final ResourceLoader resourceLoader;
    private final String apiKey;
    private final String baseUrl;

    public GeminiService(WebClient.Builder webClientBuilder,
                         ResourceLoader resourceLoader,
                         @Value("${gemini.api.key}") String apiKey,
                         @Value("${gemini.api.url}") String baseUrl) {
        this.apiKey = apiKey.trim();
        this.baseUrl = baseUrl.trim();
        this.resourceLoader = resourceLoader;
        this.webClient = webClientBuilder.build();
    }

    public Flux<String> streamQuestion(String sessionId, InterviewRequestDto request) {
        log.info("=================================================");
        log.info("[디버그] 전달받은 직무: {}, 연차: {}", request.getJobRole(), request.getExperience());
        log.info("[디버그] 전달받은 자기소개서 본문:\n{}",
                request.getResumeContent() != null ? request.getResumeContent() : "없음 (선택 안함)");
        log.info("[디버그] 전달받은 채용 공고 본문:\n{}",
                request.getJobDescription() != null ? request.getJobDescription() : "없음 (선택 안함)");
        log.info("=================================================");
        String rootUrl = baseUrl.split("/v1")[0];
        if (rootUrl.endsWith("/")) rootUrl = rootUrl.substring(0, rootUrl.length() - 1);

        String model = (request.getModelVariant() != null && !request.getModelVariant().isEmpty())
                ? request.getModelVariant() : "gemini-flash-latest";

        String fullUrl = String.format("%s/v1beta/models/%s:streamGenerateContent?alt=sse", rootUrl, model);

        String atmospherePrompt = loadPromptFile(request.getInterviewMode().name());
        String constraints = loadPromptFile("constraints");

        // [FR-INT-06] 이력서 기반 꼬리 질문 생성을 위한 컨텍스트 동적 주입
        String resumeContext = "";
        if (request.getResumeContent() != null && !request.getResumeContent().isBlank()) {
            resumeContext = "\n\n[Candidate's Resume]\n다음은 지원자의 자기소개서 내용입니다. 이를 바탕으로 지원자의 경험을 묻는 꼬리 질문을 생성하세요.\n" + request.getResumeContent();
        }

        // [FR-INT-07] 채용 공고 기반 맞춤형 질문 생성을 위한 컨텍스트 동적 주입
        String jobContext = "";
        if (request.getJobDescription() != null && !request.getJobDescription().isBlank()) {
            jobContext = "\n\n[Job Posting Requirements]\n다음은 지원자가 지원한 채용 공고의 상세 내용(요구 역량 및 주요 업무)입니다. 이를 바탕으로 직무 적합성을 검증하는 질문을 생성하세요.\n" + request.getJobDescription();
        }

        // [리뷰 반영] 하드코딩된 switch문을 제거하고 Enum을 활용하여 타입 안정성 확보
        String levelInstruction = ExperienceLevel.from(request.getExperience()).getInstruction();
        String roleInstruction = JobRole.from(request.getJobRole()).getInstruction();

        // [수정] SystemPrompt 조립 시 동적 지시사항 병합
        String systemPrompt = String.format("%s\n\n=== [지원자 맞춤형 지시사항] ===\n%s\n%s\n%s%s\n\n%s",
                atmospherePrompt,
                roleInstruction,
                levelInstruction,
                resumeContext,
                jobContext,
                constraints);

        // 2. 순수한 사용자 입력값 분리
        String userMessage = request.getMessage();

        log.info("[Gemini-Streaming] Session: {}, Mode: {}", sessionId, request.getInterviewMode());

        Map<String, Object> requestBody = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of("role", "user",
                                "parts", List.of(Map.of("text", userMessage)))
                )
        );

        log.info("[디버그] 제미나이에게 전송되는 최종 Payload (JSON)");
        log.info("{}", requestBody);
        log.info("=======================================================================");

        return webClient.post()
                .uri(URI.create(fullUrl))
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(e -> log.error("=== 🚨 Gemini API 호출 에러: {} ===", e.getMessage()));
    }

    // Path Traversal 방어를 위한 허용 목록(Allow-list) 검증
    private String loadPromptFile(String fileName) {
        final String currentFileName = fileName;

        boolean isAllowed = Arrays.stream(InterviewMode.values())
                .anyMatch(mode -> mode.name().equals(currentFileName)) || "constraints".equals(currentFileName);

        String targetFileName = fileName;
        if (!isAllowed) {
            log.error("보안 위협: 허용되지 않은 파일 이름 접근 시도 - {}", fileName);
            targetFileName = "NORMAL";
        }

        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/" + targetFileName + ".txt");
            if (!resource.exists()) {
                log.warn("프롬프트 파일이 존재하지 않아 기본 설정을 로드합니다: {}", targetFileName);
                resource = resourceLoader.getResource("classpath:prompts/NORMAL.txt");
            }
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("프롬프트 파일 로드 실패 [파일명: {}]: ", targetFileName, e);
            return "면접관으로서 질문을 생성하세요.";
        }
    }
}