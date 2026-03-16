package com.aibe.team2.domain.mypage.service;

import com.aibe.team2.domain.interview.repository.InterviewSessionRepository;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.mypage.dto.response.RecentActivityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 단순 조회이므로 readOnly = true로 성능 최적화
public class RecentActivityService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final InterviewSessionRepository interviewSessionRepository;

    public List<RecentActivityResponse> getRecentActivities(Long memberId) {
        List<RecentActivityResponse> combinedActivities = new ArrayList<>();

        // 1. 최신 자소서 5개 가져와서 DTO로 변환
        var resumeActivities = resumeAnalysisRepository.findTop5ByResume_MemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(report -> new RecentActivityResponse(
                        report.getId(),
                        "RESUME",
                        report.getResume().getTitle(), // 자소서 제목
                        report.getCreatedAt(),
                        null // 자소서는 점수가 없으므로 null
                ));

        // 2. 최신 모의 면접 5개 가져와서 DTO로 변환
        var interviewActivities = interviewSessionRepository.findTop5ByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(session -> new RecentActivityResponse(
                        session.getId(),
                        "INTERVIEW",
                        session.getInterviewType(), // 예: "심층 면접"
                        session.getCreatedAt(),
                        session.getFinalScore() // 최종 점수
                ));

        // 3. 두 리스트를 합친 후(최대 10개), 시간을 기준으로 최신순 정렬하고 최종 상위 5개만 잘라서 반환
        return Stream.concat(resumeActivities, interviewActivities)
                .sorted(Comparator.comparing(RecentActivityResponse::createdAt).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }
}
