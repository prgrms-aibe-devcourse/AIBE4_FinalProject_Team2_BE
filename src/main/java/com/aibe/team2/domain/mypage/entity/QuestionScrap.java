package com.aibe.team2.domain.mypage.entity;

import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "question_scrap",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_interview_record",
                        columnNames = {"member_id", "interview_record_id"}
                )
        }
)
public class QuestionScrap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 북마크했는지 N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 어떤 질문을 북마크했는지 N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_record_id", nullable = false)
    private InterviewRecord interviewRecord;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public QuestionScrap(Member member, InterviewRecord interviewRecord, String memo) {
        this.member = member;
        this.interviewRecord = interviewRecord;
        this.memo = memo;
    }

    // 메모 수정 기능
    public void updateMemo(String memo){
        this.memo = memo;
    }
}
