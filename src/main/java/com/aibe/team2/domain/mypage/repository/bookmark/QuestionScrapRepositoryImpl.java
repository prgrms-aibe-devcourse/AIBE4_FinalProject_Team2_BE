package com.aibe.team2.domain.mypage.repository.bookmark;

import com.aibe.team2.domain.mypage.entity.QuestionScrap;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.aibe.team2.domain.interview.entity.QInterviewSession.interviewSession;
import static com.aibe.team2.domain.mypage.entity.QQuestionScrap.questionScrap;
import static com.aibe.team2.domain.statistics.entity.QInterviewRecord.interviewRecord;

@RequiredArgsConstructor
public class QuestionScrapRepositoryImpl implements QuestionScrapRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<QuestionScrap> findScrapsByMemberId(Long memberId, Pageable pageable){

        // 1. 컨텐츠 조회(Fetch Join 적용)
        List<QuestionScrap> questionScraps = queryFactory
                .selectFrom(questionScrap)
                // [N+1 해결] 북마크 -> 면접기록 -> 세션까지 한 번에 당겨오기
                .leftJoin(questionScrap.interviewRecord, interviewRecord).fetchJoin()
                .leftJoin(interviewRecord.interviewSession, interviewSession).fetchJoin()
                .where(questionScrap.member.memberId.eq(memberId)) // 내 북마크만
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(questionScrap.createdAt.desc()) // 최신순 정렬
                .fetch();

        Long total = queryFactory
                .select(questionScrap.count())
                .from(questionScrap)
                .where(questionScrap.member.memberId.eq(memberId))
                .fetchOne();

        long totalCount = (total != null) ? total : 0;

        return new PageImpl<>(questionScraps, pageable, totalCount);
    }
}
