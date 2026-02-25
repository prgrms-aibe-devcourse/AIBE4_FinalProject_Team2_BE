package com.aibe.team2.domain.mypage.repository.bookmark;

import com.aibe.team2.domain.mypage.entity.QuestionScrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionScrapRepositoryCustom {
    Page<QuestionScrap> findScrapsByMemberId(Long memberId, Pageable pageable);
}
