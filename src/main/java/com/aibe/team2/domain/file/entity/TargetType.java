package com.aibe.team2.domain.file.entity;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;

public enum TargetType {
    RESUME,
    ANALYSIS_REPORT,
    INTERVIEW_RECORD;

    public static TargetType from(String value) {
        try {
            return TargetType.valueOf(value);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.COMMON_408);
        }
    }
}