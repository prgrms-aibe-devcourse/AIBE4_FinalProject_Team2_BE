package com.aibe.team2.domain.statistics.entity;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.global.converter.JsonAttributeConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "interview_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class InterviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 다대일 연관관계 매핑(FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_session_id", nullable = false)
    private InterviewSession interviewSession;

    @Column(name = "turn_sequence")
    private Integer turnSequence; // 대화 순서 (Turn)

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText; // 질문 내용

    @Column(name = "question_intent", columnDefinition = "TEXT")
    private String questionIntent; // 질문 의도

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText; // 답변 내용

    @Column(name = "follow_up_depth")
    private Integer followUpDepth; // 꼬리질문 깊이

    @Column(name = "s3_file_url", length = 500)
    private String s3FileUrl; // 답변 음성 파일 URL

    @Column(name = "wpm")
    private Integer wpm; // 발화 속도 (Words Per Minute)

    @Column(name = "stt_accuracy")
    private Float sttAccuracy; // STT 정확도

    @Column(name = "silence_count")
    private Integer silenceCount; // 침묵 횟수

    // 감정 분석 결과 JSON 데이터 (컨버터 재사용)
    @Convert(converter = JsonAttributeConverter.class)
    @Column(name = "emotion_analysis", columnDefinition = "TEXT")
    private Map<String, Object> emotionAnalysis;

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText; // 피드백 내용

    @Column(name = "evaluation_score")
    private Float evaluationScore; // 답변 평가 점수

    @Column(name = "response_time_ms")
    private Integer responseTimeMs; // 응답 소요 시간 (ms)

    // [추가] AI 평가 사고 과정 (CoT)
    @Column(name = "evaluation_reason", columnDefinition = "TEXT")
    private String evaluationReason;

    // [추가] 모범 답변 가이드 키워드 (JSON이나 TEXT 형태로 저장)
    @Column(name = "recommended_guides", columnDefinition = "TEXT")
    private String recommendedGuides;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // [수정] 평가 점수, 피드백뿐만 아니라 이유와 가이드도 함께 업데이트하도록 메서드 확장
    public void updateAIAnalysis(Float score, String feedback, String reason, String guides) {
        this.evaluationScore = score;
        this.feedbackText = feedback;
        this.evaluationReason = reason;
        this.recommendedGuides = guides;
    }

    // [수정] 빌더에 새 필드 2개 추가
    @Builder
    public InterviewRecord(
            InterviewSession interviewSession, Integer turnSequence, String questionText,
            String questionIntent, String answerText, Integer followUpDepth,
            String s3FileUrl, Integer wpm, Float sttAccuracy, Integer silenceCount,
            Map<String, Object> emotionAnalysis, String feedbackText,
            Float evaluationScore, Integer responseTimeMs,
            String evaluationReason, String recommendedGuides // 추가됨
    ){
        this.interviewSession = interviewSession;
        this.turnSequence = turnSequence;
        this.questionText = questionText;
        this.questionIntent = questionIntent;
        this.answerText = answerText;
        this.followUpDepth = followUpDepth;
        this.s3FileUrl = s3FileUrl;
        this.wpm = wpm;
        this.sttAccuracy = sttAccuracy;
        this.silenceCount = silenceCount;
        this.emotionAnalysis = emotionAnalysis;
        this.feedbackText = feedbackText;
        this.evaluationScore = evaluationScore;
        this.responseTimeMs = responseTimeMs;
        this.evaluationReason = evaluationReason;       // 추가됨
        this.recommendedGuides = recommendedGuides;     // 추가됨
    }
}