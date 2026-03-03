package com.aibe.team2.global.redis.lock;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@Order(1) // @Transactional보다 먼저 실행되도록 우선순위 지정
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(com.aibe.team2.global.redis.lock.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DistributedLock distributedLock = signature.getMethod().getAnnotation(DistributedLock.class);

        // 파라미터 값들을 조합하여 동적 Key 생성 (예: resume-analysis:15:2)
        String dynamicKey = getDynamicKeyFromArgs(joinPoint.getArgs());
        String lockKey = distributedLock.key() + ":" + dynamicKey;

        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("[DistributedLock] 락 획득 실패. 중복 요청 방어됨. key: {}", lockKey);
                // 중복 요청 시 예외 발생 (400 또는 409 에러)
                throw new BusinessException(ErrorCode.COMMON_409);
            }

            // 락 획득 성공 시 비즈니스 로직 실행
            return joinPoint.proceed();

        } catch (InterruptedException e) {
            log.error("[DistributedLock] 락 대기 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[DistributedLock] 락 반환 완료. key: {}", lockKey);
            }
        }
    }

    private String getDynamicKeyFromArgs(Object[] args) {
        if (args == null || args.length == 0) return "default";
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            sb.append(arg).append(":");
        }
        return sb.substring(0, sb.length() - 1);
    }
}