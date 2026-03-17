package com.aibe.team2.domain.error.util;

import com.aibe.team2.domain.error.enums.ErrorSeverity;
import org.springframework.stereotype.Component;

@Component
public class ErrorSeverityResolver {

    public ErrorSeverity resolve(String errorCode, Throwable throwable) {
        String exceptionName = throwable != null ? throwable.getClass().getSimpleName() : "";

        if (exceptionName.contains("NullPointerException")
                || exceptionName.contains("OutOfMemoryError")) {
            return ErrorSeverity.CRITICAL;
        }

        if (exceptionName.contains("IllegalStateException")
                || exceptionName.contains("SQLException")) {
            return ErrorSeverity.HIGH;
        }

        if (errorCode != null && errorCode.startsWith("AUTH")) {
            return ErrorSeverity.MEDIUM;
        }

        return ErrorSeverity.LOW;
    }
}