package com.aibe.team2.domain.mypage.dto.response;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.mypage.entity.QuestionScrap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BookmarkResponse {

    private Long scrapId;           // 북마크 고유 ID (삭제 시 필요)
    private Long questionId;        // 원본 질문 ID (InterviewRecord ID)
    private String questionText;    // 질문 내용
    private String companyName;     // 회사명 (채용공고가 있다면)
    private String jobTitle;        // 직무명
    private Long linkedInterviewId; // [핵심] 상세 페이지 이동용 "티켓" (세션 ID)
    private LocalDateTime createdAt;// 북마크한 날짜

    public static BookmarkResponse from(QuestionScrap scrap, JobPosting jobPosting) {
        var record = scrap.getInterviewRecord();
        var session = record.getInterviewSession();

        String company = (jobPosting != null) ? jobPosting.getCompanyName() : "자유 연습";
        String job = (jobPosting != null) ? jobPosting.getJobTitle() : "General";

        return BookmarkResponse.builder()
                .scrapId(scrap.getId())
                .questionId(record.getId())
                .questionText(record.getQuestionText())
                .companyName(company)
                .jobTitle(job)
                .linkedInterviewId(session.getId())
                .createdAt(scrap.getCreatedAt())
                .build();
    }
}
