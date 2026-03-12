package com.aibe.team2.domain.interview.repository;

import com.aibe.team2.domain.interview.enums.InterviewSessionStatus; // [FR-INT-09] 상태 Enum 임포트
import com.aibe.team2.domain.interview.enums.InterviewType;
import com.aibe.team2.domain.mypage.dto.response.InterviewSessionListResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.aibe.team2.domain.interview.entity.QInterviewSession.interviewSession;
import static com.aibe.team2.domain.jobposting.entity.QJobPosting.jobPosting;
import static com.aibe.team2.domain.resume.entity.QResume.resume;

@RequiredArgsConstructor
public class InterviewSessionRepositoryImpl implements InterviewSessionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<InterviewSessionListResponse> findInterviewSessionList(
            Long memberId,
            InterviewType type,
            String keyword,
            Pageable pageable
    ) {
        // 1. 공통 조건(from, join, where)을 포함하는 기본 쿼리 생성
        JPAQuery<?> baseQuery = queryFactory
                .from(interviewSession)
                .leftJoin(resume).on(interviewSession.resumeId.eq(resume.id))
                .leftJoin(jobPosting).on(interviewSession.jobPostingId.eq(jobPosting.id))
                .where(
                        interviewSession.memberId.eq(memberId),
                        interviewSession.status.ne(InterviewSessionStatus.ABORTED), // [FR-INT-09] 비정상 종료 세션 제외 조건 추가
                        eqType(type),
                        containsKeyword(keyword)
                );

        // 2. 실제 데이터 조회 쿼리 후 곧바로 List 반환
        return baseQuery.clone()
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
                .orderBy(interviewSession.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch(); // fetch()는 List를 반환합니다.
    }

    // 검색어 포함 여부 확인
    private BooleanExpression containsKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null; // 검색어가 없으면 조건을 무시
        }
        // 예시: 회사명 또는 자기소개서 제목에 검색어가 포함되어 있는지 검사
        return jobPosting.companyName.contains(keyword)
                .or(resume.title.contains(keyword));
    }

    private BooleanExpression eqType(InterviewType type) {
        if (type == null) {
            return null; // 프론트에서 값이 안 오면(전체 조회 시) 조건을 무시
        }

        // 1. 현재 엔티티의 필드가 String 타입이므로, Enum 객체를 String으로 변환(type.name())하여 비교합니다.
        // 2. 나중을 위해 기술 부채(Technical Debt)를 기록해 둡니다.
        // TODO: 향후 InterviewSession 엔티티의 interviewType 필드를 Enum으로 변경 후 eq(type)으로 리팩토링 필요
        return interviewSession.interviewType.eq(type.name());
    }
}