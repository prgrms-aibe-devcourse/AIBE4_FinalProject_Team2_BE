package com.aibe.team2.domain.interview.repository;

import com.aibe.team2.domain.interview.enums.InterviewType;
import com.aibe.team2.domain.mypage.dto.response.InterviewSessionListResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.aibe.team2.domain.interview.entity.QInterviewSession.interviewSession;
import static com.aibe.team2.domain.jobposting.entity.QJobPosting.jobPosting;
import static com.aibe.team2.domain.resume.entity.QResume.resume;

@RequiredArgsConstructor
public class InterviewSessionRepositoryImpl implements InterviewSessionRepositoryCustom {

    private final JPAQueryFactory  queryFactory;

    @Override
    public Page<InterviewSessionListResponse> findInterviewSessionList(
            Long memberId,
            InterviewType type,
            String keyword,
            Pageable pageable
    ) {

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
                .where(
                        interviewSession.memberId.eq(memberId),
                        eqType(type),              // 모드 필터 (VOICE, TEXT)
                        containsKeyword(keyword)   // 검색어 필터
                )
                .orderBy(interviewSession.createdAt.desc()) // 최신순 정렬
                .offset(pageable.getOffset())               // 페이징 시작점
                .limit(pageable.getPageSize())              // 페이지 크기
                .fetch();

        // 2. 전체 개수 조회 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(interviewSession.count())
                .from(interviewSession)
                // ✨ [수정됨] 검색어 필터링을 위해 JOIN이 필요하므로 카운트 쿼리에도 추가
                .leftJoin(resume).on(interviewSession.resumeId.eq(resume.id))
                .leftJoin(jobPosting).on(interviewSession.jobPostingId.eq(jobPosting.id))
                // ✨ [수정됨] 데이터 쿼리와 완벽하게 동일한 조건 적용
                .where(
                        interviewSession.memberId.eq(memberId),
                        eqType(type),
                        containsKeyword(keyword)
                );

        // 3. 데이터와 카운트 쿼리를 조합하여 Page 객체로 반환
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // 면접 모드(VOICE, TEXT) 일치 여부 확인
    private BooleanExpression eqType(InterviewType type) {
        if (type == null) {
            return null; // 프론트에서 값이 안 오면(전체 조회 시) 조건을 무시
        }
        return interviewSession.interviewType.eq(type.name());
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
}
