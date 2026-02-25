package com.aibe.team2.domain.mypage.controller;

import com.aibe.team2.domain.mypage.dto.response.BookmarkResponse;
import com.aibe.team2.domain.mypage.service.QuestionScrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "마이페이지 - 북마크", description = "질문 북마크 관리 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/mypage")
public class QuestionScrapController {

    private final QuestionScrapService questionScrapService;
    // TODO : Spring Security 구현 뒤 삭제할 하드코딩
    private final Long TEST_MEMBER_ID = 1L;

    /*
     * [북마크 토글]
     * POST /api/v1/mypage/questions/{questionId}/bookmarks
     */
    @Operation(summary = "질문 북마크 토글", description = "=이미 저장된 질문이면 삭제하고, 없으면 저장합니다. (Return: true = 저장됨, false삭제됨)")
    @PostMapping("/questions/{questionId}/bookmarks")
    public ResponseEntity<Boolean> toggleBookmark(
            @PathVariable Long questionId
            // @AuthenticationPrincipal UserDetails userDetails
    ){
        // 1. 임시로 DI 고정
        Long memberId = TEST_MEMBER_ID;
        log.info("[북마크 토글 요청] MemberId: {}, QuestionId: {}", memberId, questionId);
        // API 명세상 변수명은 questionId지만, 비즈니스 로직상 면접기록(InterviewRecord) ID로 사용됨
        boolean isBookmarked = questionScrapService.toggleBookmark(memberId, questionId);

        return ResponseEntity.ok(isBookmarked);
    }

    /*
     * [내 북마크 목록 조회]
     * GET /api/v1/mypage/bookmarks
     */
    @Operation(summary = "내 북마크 목록 조회", description = "내가 저장한 질문 리스트를 페이징하여 조회합니다.")
    @GetMapping("/bookmarks")
    public ResponseEntity<Page<BookmarkResponse>> getMyBookmarks(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long memberId = TEST_MEMBER_ID;
        log.info("[북마크 목록 조회] MemberId: {}",  memberId);
        Page<BookmarkResponse> response = questionScrapService.getMyBookmarks(memberId, pageable);

        return ResponseEntity.ok(response);
    }
}
