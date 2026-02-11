package com.aibe.team2.global.exception.custom;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;

// 파일 업로드/다운로드 관련
public class FileException extends BusinessException {
    public FileException(ErrorCode errorCode) {
        super(errorCode);
    }
}