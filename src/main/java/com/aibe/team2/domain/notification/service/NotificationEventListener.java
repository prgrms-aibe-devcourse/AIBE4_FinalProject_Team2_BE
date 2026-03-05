package com.aibe.team2.domain.notification.service;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.notification.event.ResumeAnalysisCompleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeAnalysisComplete(ResumeAnalysisCompleteEvent event){
        log.info("[Notification] 이력서 분석 완료 이벤트를 수신했습니다. 알림을 발송합니다. memberId: {}", event.memberId());

        Member dummyMember = Member.builder()
                .memberId(event.memberId())
                .build();

        String type = "AI_ANALYSIS_COMPLETE";
        String message = "이력서 AI 분석이 성공적으로 완료되었습니다! 결과를 확인해보세요!";

        notificationService.send(dummyMember, type, message);
    }
}
