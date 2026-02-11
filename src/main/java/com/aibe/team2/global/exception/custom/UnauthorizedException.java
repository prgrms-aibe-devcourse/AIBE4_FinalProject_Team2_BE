package com.aibe.team2.global.exception.custom;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;

// 401 Unauthorized (로그인 실패, 토큰 만료 등)
public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}