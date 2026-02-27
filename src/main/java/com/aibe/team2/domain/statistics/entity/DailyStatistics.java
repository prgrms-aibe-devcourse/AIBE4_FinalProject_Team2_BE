package com.aibe.team2.domain.statistics.entity;

import com.aibe.team2.domain.mypage.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "daily_statistics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_statistics_member_date",
                        columnNames = {"member_id", "stats_date"}
                )
        })
public class DailyStatistics {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Member 엔티티와 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "stats_date", nullable = false)
    private LocalDate statsDate;

    @Column(name = "total_resume_count", nullable = false)
    private int totalResumeCount;

    @Column(name = "total_interview_count", nullable = false)
    private int totalInterviewCount;

    @Builder
    public DailyStatistics(Member member, LocalDate statsDate, int totalResumeCount, int totalInterviewCount) {
        this.member = member;
        this.statsDate = statsDate;
        this.totalResumeCount = totalResumeCount;
        this.totalInterviewCount = totalInterviewCount;
    }

    public void updateCounts(int resumeCount, int interviewCount) {
        this.totalResumeCount = resumeCount;
        this.totalInterviewCount = interviewCount;
    }
}
