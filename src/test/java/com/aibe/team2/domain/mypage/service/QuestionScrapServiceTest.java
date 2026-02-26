package com.aibe.team2.domain.mypage.service;

import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.domain.mypage.dto.response.BookmarkResponse;
import com.aibe.team2.domain.mypage.entity.QuestionScrap;
import com.aibe.team2.domain.mypage.repository.bookmark.QuestionScrapRepository;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.statistics.repository.interview.InterviewRecordRepository;
import com.aibe.team2.global.exception.custom.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Mockito 환경에서 실행
class QuestionScrapServiceTest {

    @InjectMocks
    private QuestionScrapService questionScrapService; // 테스트 대상 (가짜들이 주입됨)

    @Mock
    private QuestionScrapRepository questionScrapRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private InterviewRecordRepository interviewRecordRepository;
    @Mock
    private JobPostingRepository jobPostingRepository;

    // --- 테스트용 더미 데이터 생성 메서드들 ---
    private Member createMember() {
        Member member = Member.builder()
                .nickname("tester")
                .build();
        // "memberId" 필드에 1L을 강제로 넣습니다. (빌더 에러 방지)
        ReflectionTestUtils.setField(member, "memberId", 1L);
        return member;
    }

    private InterviewRecord createRecord() {
        InterviewRecord record = InterviewRecord.builder()
                .questionText("Test Question")
                .build();
        // "id" 필드에 10L을 강제로 넣습니다.
        ReflectionTestUtils.setField(record, "id", 10L);
        return record;
    }

    @Test
    @DisplayName("북마크 생성: 기존에 없으면 저장하고 true 반환")
    void toggleBookmark_save() {
        // given (준비)
        Long memberId = 1L;
        Long recordId = 10L;
        Member member = createMember();
        InterviewRecord record = createRecord();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(interviewRecordRepository.findById(recordId)).willReturn(Optional.of(record));
        // DB에 북마크가 없는 상황 연출 (Empty)
        given(questionScrapRepository.findByMemberAndInterviewRecord(member, record))
                .willReturn(Optional.empty());

        // when (실행)
        boolean result = questionScrapService.toggleBookmark(memberId, recordId);

        // then (검증)
        assertThat(result).isTrue(); // 결과는 true여야 함
        verify(questionScrapRepository, times(1)).save(any(QuestionScrap.class)); // save가 1번 호출됐는지 확인
    }

    @Test
    @DisplayName("북마크 취소: 이미 있으면 삭제하고 false 반환")
    void toggleBookmark_delete() {
        // given
        Long memberId = 1L;
        Long recordId = 10L;
        Member member = createMember();
        InterviewRecord record = createRecord();
        QuestionScrap existingScrap = QuestionScrap.builder().member(member).interviewRecord(record).build();

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(interviewRecordRepository.findById(recordId)).willReturn(Optional.of(record));
        // DB에 북마크가 이미 있는 상황 연출 (Present)
        given(questionScrapRepository.findByMemberAndInterviewRecord(member, record))
                .willReturn(Optional.of(existingScrap));

        // when
        boolean result = questionScrapService.toggleBookmark(memberId, recordId);

        // then
        assertThat(result).isFalse(); // 결과는 false여야 함
        verify(questionScrapRepository, times(1)).delete(existingScrap); // delete가 1번 호출됐는지 확인
    }

    @Test
    @DisplayName("예외 발생: 회원이 존재하지 않을 때")
    void toggleBookmark_memberNotFound() {
        // given
        Long memberId = 99L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then (실행 시 예외가 터져야 성공)
        assertThrows(NotFoundException.class, () ->
                questionScrapService.toggleBookmark(memberId, 10L)
        );
    }

    @Test
    @DisplayName("북마크 목록 조회: 데이터가 있을 때 DTO로 잘 변환되는지")
    void getMyBookmarks_success() {
        // given
        Long memberId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        // 1. 가짜 객체들 생성 (Scrap, Record, Session)
        QuestionScrap scrap = org.mockito.Mockito.mock(QuestionScrap.class);
        InterviewRecord record = org.mockito.Mockito.mock(InterviewRecord.class);
        // [추가] 세션 객체도 Mock으로 만듭니다.
        com.aibe.team2.domain.interview.entity.InterviewSession session =
                org.mockito.Mockito.mock(com.aibe.team2.domain.interview.entity.InterviewSession.class);

        // 2. 가짜 객체들끼리 연결 (Chaining)
        // "scrap.getInterviewRecord() 하면 record 줘"
        given(scrap.getInterviewRecord()).willReturn(record);

        // [핵심 해결] "record.getInterviewSession() 하면 session 줘" (이게 빠져서 NPE 발생함)
        given(record.getInterviewSession()).willReturn(session);

        // "session.getJobPostingId() 하면 100번 줘" (서비스 로직이 이걸 씁니다)
        given(session.getJobPostingId()).willReturn(100L);

        // 3. DTO 변환에 필요한 나머지 데이터 설정
        given(scrap.getId()).willReturn(1L);
        given(scrap.getCreatedAt()).willReturn(java.time.LocalDateTime.now());

        // DTO 내부에서 호출하는 getter들 처리
        given(record.getId()).willReturn(50L);
        given(record.getQuestionText()).willReturn("Test Question?");
        given(session.getId()).willReturn(200L); // linkedInterviewId

        // 4. 레포지토리 반환값 설정
        Page<QuestionScrap> scrapPage = new PageImpl<>(List.of(scrap));
        given(questionScrapRepository.findScrapsByMemberId(memberId, pageable)).willReturn(scrapPage);

        // when
        Page<BookmarkResponse> result = questionScrapService.getMyBookmarks(memberId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}