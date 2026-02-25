package com.aibe.team2.domain.mypage.repository.bookmark;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.QuestionScrap;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionScrapRepository extends JpaRepository<QuestionScrap, Long>, QuestionScrapRepositoryCustom {

    boolean existsByMemberAndInterviewRecord(Member member, InterviewRecord interviewRecord);

    Optional<QuestionScrap> findByMemberAndInterviewRecord(Member member, InterviewRecord interviewRecord);
}