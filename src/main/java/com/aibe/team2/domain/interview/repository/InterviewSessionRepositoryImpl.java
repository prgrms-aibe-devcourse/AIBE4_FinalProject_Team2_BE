package com.aibe.team2.domain.interview.repository;

import com.aibe.team2.domain.mypage.dto.InterviewSessionListResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.aibe.team2.domain.interview.entity.QInterviewSession.interviewSession;
import static com.aibe.team2.domain.jobposting.entity.QJobPosting.jobPosting;
import static com.aibe.team2.domain.resume.entity.QResume.resume;

@RequiredArgsConstructor
public class InterviewSessionRepositoryImpl implements InterviewSessionRepositoryCustom {

    private final JPAQueryFactory  queryFactory;

    @Override
    public Page<InterviewSessionListResponse> findInterviewSessionList(Long memberId, Pageable pageable) {

        // 1. 실제 데이터 조회 쿼리
        List<InterviewSessionListResponse> content = queryFactory
                .select(Projections.constructor(InterviewSessionListResponse.class,
                        interviewSession.id,
                        resume.title,
                        jobPosting.companyName,
                        jobPosting.jobTitle,
                        interviewSession.interviewMode,
                        interviewSession.interviewType,
                        interviewSession.status,
                        interviewSession.finalScore,
                        interviewSession.createdAt
                ))
                .from(interviewSession)
                // [핵심] 엔티티에 연관관계가 없으므로 ON 절로 ID 값을 직접 매칭
                .leftJoin(resume).on(interviewSession.resumeId.eq(resume.id))
                .leftJoin(jobPosting).on(interviewSession.jobPostingId.eq(jobPosting.id))
                .where(interviewSession.memberId.eq(memberId))
                .orderBy(interviewSession.createdAt.desc()) // 최신순 정렬
                .offset(pageable.getOffset())               // 페이징 시작점
                .limit(pageable.getPageSize())              // 페이지 크기
                .fetch();

        // 2. 전체 개수 조회 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(interviewSession.count())
                .from(interviewSession)
                .where(interviewSession.memberId.eq(memberId));

        // 3. 데이터와 카운트 쿼리를 조합하여 Page 객체로 반환
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
