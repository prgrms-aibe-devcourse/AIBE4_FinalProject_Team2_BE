package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.InterviewRequestDto;
import com.aibe.team2.domain.interview.enums.InterviewMode;
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
        log.info("👀 [디버그] 전달받은 직무: {}, 연차: {}", request.getJobRole(), request.getExperience());
        log.info("👀 [디버그] 전달받은 자기소개서 본문:\n{}",
                request.getResumeContent() != null ? request.getResumeContent() : "없음 (선택 안함)");
        log.info("👀 [디버그] 전달받은 채용 공고 본문:\n{}",
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

        // [추가] 직무 및 연차 기반 맞춤형 지시사항 동적 생성
        String levelInstruction = "";
        if (request.getExperience() != null) {
            levelInstruction = switch (request.getExperience()) {
                case "NEWBIE" -> "지원자는 '신입(경력 없음)'입니다. 너무 깊은 아키텍처 질문은 피하고, 기초적인 전공 지식, 열정, 문제 해결을 위한 접근 방식, 그리고 잠재력을 위주로 평가하세요.";
                case "JUNIOR" -> "지원자는 '주니어(1~3년 차)'입니다. 실무에서의 구체적인 트러블슈팅 경험, 협업 태도, 그리고 기본 기술 스택 활용 능력을 검증하세요.";
                case "MIDDLE" -> "지원자는 '미들(4~7년 차)'입니다. 시스템 아키텍처 설계, 대규모 데이터 성능 최적화 경험 등 심도 있는 기술 역량을 강하게 검증하세요.";
                case "SENIOR" -> "지원자는 '시니어(8년 차 이상)'입니다. 기술 스택 결정 권한, 프로젝트 리딩 경험, 아키텍처 설계 의도 및 비즈니스 임팩트에 대한 날카로운 질문을 던지세요.";
                default -> "지원자의 경력에 맞는 적절한 질문을 던지세요.";
            };
        }

        String roleInstruction = "";
        if (request.getJobRole() != null) {
            roleInstruction = switch (request.getJobRole()) {
                case "BACKEND" -> "지원자의 희망 직무는 '백엔드 엔지니어'입니다. 서버, 데이터베이스, API 설계, 트랜잭션, 성능 최적화 관련 기술 질문을 우선적으로 던지세요.";
                case "FRONTEND" -> "지원자의 희망 직무는 '프론트엔드 엔지니어'입니다. 렌더링 최적화, 상태 관리, 브라우저 동작 원리, UI/UX 구현 관련 기술 질문을 우선적으로 던지세요.";
                case "FULLSTACK" -> "지원자의 희망 직무는 '풀스택 엔지니어'입니다. 프론트엔드부터 백엔드, DB까지 아우르는 전체적인 시스템 이해도와 통신 과정에 대한 질문을 던지세요.";
                case "PM" -> "지원자의 희망 직무는 '서비스 기획자(PM/PO)'입니다. 데이터 기반 의사결정, 일정 및 리소스 관리, 개발자와의 협업 및 갈등 해결 경험에 대한 질문을 던지세요.";
                default -> "지원자의 희망 직무에 특화된 기술적 질문을 우선적으로 던지세요.";
            };
        }

        // 🚀 [수정] SystemPrompt 조립 시 동적 지시사항 병합 (순서 중요: 페르소나 -> 맞춤형 지시 -> 이력서/공고 -> 제약조건)
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

        // JSON Payload 조립 시 systemInstruction 속성을 명시적으로 분리하여 전송
        Map<String, Object> requestBody = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of("role", "user",
                                "parts", List.of(Map.of("text", userMessage)))
                )
        );

        // [디버그용 로그 추가] Gemini API로 날아가기 직전의 최종 캡슐(JSON) 확인
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

        // 허용 목록 검증: Enum 상수에 있거나 'constraints'인 경우만 허용
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
            log.error("❌ 프롬프트 파일 로드 실패 [파일명: {}]: ", targetFileName, e);
            return "면접관으로서 질문을 생성하세요.";
        }
    }
}