package com.aibe.team2.domain.mypage.service;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.mypage.dto.response.BookmarkResponse;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.QuestionScrap;
import com.aibe.team2.domain.mypage.repository.bookmark.QuestionScrapRepository;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.statistics.repository.interview.InterviewRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /*
     * [북마크 토글 기능]
     * - 이미 북마크 되어있으면 -> 삭제 (return false)
     * - 북마크 안 되어있으면 -> 저장 (return true)
     */
    @Transactional
    public boolean toggleBookmark(Long memberId, Long interviewRecordId){

        // 1. 사용자 조회(없으면 예외 발생)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 질문 조회
        InterviewRecord interviewRecord = interviewRecordRepository.findById(interviewRecordId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 면접 기록입니다."));

        // 3. 북마크 존재 여부 확인 및 처리
        return questionScrapRepository.findByMemberAndInterviewRecord(member, interviewRecord)
                .map(scrap -> {
                    // 3-1. 이미 존재하면 삭제
                    questionScrapRepository.delete(scrap);
                    return false; // 북마크 해제
                })
                .orElseGet(() -> {
                    // 3-2. 없으면 새로 생성 및 저장
                    QuestionScrap newScrap = QuestionScrap.builder()
                            .member(member)
                            .interviewRecord(interviewRecord)
                            .build();
                    questionScrapRepository.save(newScrap);
                    return true; // 북마크 설정됨
                });
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
}
