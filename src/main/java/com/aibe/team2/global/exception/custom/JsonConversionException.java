package com.aibe.team2.global.exception.custom;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;

public class JsonConversionException extends BusinessException {

    public JsonConversionException(ErrorCode errorCode) {
        super(errorCode);
    }
}
