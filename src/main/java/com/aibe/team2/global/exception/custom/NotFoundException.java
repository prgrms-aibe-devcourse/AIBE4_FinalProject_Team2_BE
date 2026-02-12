package com.aibe.team2.global.exception.custom;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;

// 404 Not Found (사용자 없음, 게시글 없음 등)
public class NotFoundException extends BusinessException {
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}