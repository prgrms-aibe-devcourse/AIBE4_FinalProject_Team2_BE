package com.aibe.team2.global.exception.custom;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;

// 400 Bad Request
public class BadRequestException extends BusinessException {
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}