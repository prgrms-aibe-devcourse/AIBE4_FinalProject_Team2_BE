package com.aibe.team2.domain.mypage.service;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.mypage.dto.response.BookmarkResponse;
import com.aibe.team2.domain.mypage.dto.response.BookmarkStatsResponse;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.QuestionScrap;
import com.aibe.team2.domain.mypage.repository.bookmark.QuestionScrapRepository;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.statistics.repository.interview.InterviewRecordRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.ForbiddenException;
import com.aibe.team2.global.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionScrapService {

    private final QuestionScrapRepository questionScrapRepository;
    private final MemberRepository memberRepository;
    private final InterviewRecordRepository interviewRecordRepository;
    private final JobPostingRepository jobPostingRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    /*
     * [북마크 토글 기능]
     * - 이미 북마크 되어있으면 -> 삭제 (return false)
     * - 북마크 안 되어있으면 -> 저장 (return true)
     */
    @Transactional
    public boolean toggleBookmark(Long memberId, Long interviewRecordId){

        // 1. 사용자 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // 2. 질문 조회
        InterviewRecord interviewRecord = interviewRecordRepository.findById(interviewRecordId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.INTERVIEW_RECORD_NOT_FOUND));

        // [Security Fix] IDOR 취약점 방지: 내 면접 기록인지 확인
        // TODO: 만약 '다른 사람의 공개된 면접 기록'을 북마크하는 기능이라면 이 부분 로직을 수정해야 합니다. (예: isPublic 체크 등)
        Long ownerId = interviewRecord.getInterviewSession().getMemberId();
        if (!ownerId.equals(memberId)) {
            // 내 것이 아니면 접근 거부
            throw new ForbiddenException(ErrorCode.INTERVIEW_OWNERSHIP_ERROR);
        }

        // Redis Key 생성
        String redisKey = "bookmark:count:" + interviewRecordId;

        // 3. 북마크 로직 + Redis 캐시 무효화(Invalidation)
        return questionScrapRepository.findByMemberAndInterviewRecord(member, interviewRecord)
                .map(scrap -> {
                    // 3-1. 삭제 로직
                    questionScrapRepository.delete(scrap);

                    // [Redis Fix] 값을 직접 줄이지 않고 캐시를 삭제함 (데이터 정합성 보장)
                    redisTemplate.delete(redisKey);

                    return false;
                })
                .orElseGet(() -> {
                    // 3-2. 저장 로직
                    QuestionScrap newScrap = QuestionScrap.builder()
                            .member(member)
                            .interviewRecord(interviewRecord)
                            .build();
                    questionScrapRepository.save(newScrap);

                    // [Redis Fix] 값을 직접 늘리지 않고 캐시를 삭제함
                    redisTemplate.delete(redisKey);

                    return true;
                });
    }

    /*
     * [북마크 개수 조회 - Redis Caching 적용]
     * - 화면에서 "좋아요 120개" 보여줄 때 사용하는 메서드입니다.
     */
    public Long getBookmarkCount(Long interviewRecordId){
        String redisKey = "bookmark:count:" + interviewRecordId;

        String countStr = (String) redisTemplate.opsForValue().get(redisKey);

        if(countStr != null) {
            return Long.parseLong(countStr);
        }

        Long dbCount = questionScrapRepository.countByInterviewRecordId(interviewRecordId);
        redisTemplate.opsForValue().set(redisKey, String.valueOf(dbCount), Duration.ofHours(1));

        return dbCount;
    }

    /*
     * [마이페이지 북마크 목록 조회]
     * - QueryDSL로 최적화된 쿼리 사용
     * - Entity -> DTO 변환
     */
    public Page<BookmarkResponse> getMyBookmarks(Long memberId, Pageable pageable) {

        // 1. 북마크 목록 조회
        Page<QuestionScrap> scrapPage = questionScrapRepository.findScrapsByMemberId(memberId, pageable);

        // 데이터가 없으면 빈 페이지 반환
        if(scrapPage.isEmpty()){
            return Page.empty(pageable);
        }

        // 2. jobPostingId 추출
        Set<Long> jobPostingIds = scrapPage.getContent().stream()
                .map(scrap -> scrap.getInterviewRecord().getInterviewSession().getJobPostingId())
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 3. 채용공고 일괄 조회(IN 쿼리 발생 -> 성능 최적화)
        Map<Long, JobPosting> jobPostingMap;

        if(jobPostingIds.isEmpty()){
            jobPostingMap = Collections.emptyMap();
        } else {
            jobPostingMap = jobPostingRepository.findAllById(jobPostingIds).stream()
                    .collect(Collectors.toMap(JobPosting::getId, Function.identity()));
        }

        // 4. DTO 변환
        return scrapPage.map(scrap -> {
            Long jobId = scrap.getInterviewRecord().getInterviewSession().getJobPostingId();
            JobPosting jobPosting = jobPostingMap.get(jobId);

            return BookmarkResponse.from(scrap, jobPosting);
        });
    }

    @Transactional(readOnly = true)
    public BookmarkStatsResponse getBookmarkStats(Long memberId) {
        // Repository에서 멤버의 북마크 총 갯수를 가져옴
        long totalBookmarks = questionScrapRepository.countByMember_MemberId(memberId);

        return new BookmarkStatsResponse(totalBookmarks);
    }
}