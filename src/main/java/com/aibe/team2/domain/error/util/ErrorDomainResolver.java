package com.aibe.team2.domain.error.util;

import com.aibe.team2.domain.error.enums.ErrorDomain;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ErrorDomainResolver {

    public ErrorDomain resolve(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri == null || uri.isBlank()) {
            return ErrorDomain.GLOBAL;
        }

        if (uri.startsWith("/api/v1/auth")) {
            return ErrorDomain.AUTH;
        }
        if (uri.startsWith("/api/v1/resume")) {
            return ErrorDomain.RESUME;
        }
        if (uri.startsWith("/api/v1/interview")) {
            return ErrorDomain.INTERVIEW;
        }
        if (uri.startsWith("/api/v1/file")) {
            return ErrorDomain.FILE;
        }
        if (uri.startsWith("/api/v1/admin")) {
            return ErrorDomain.ADMIN;
        }
        if (uri.startsWith("/api/v1/statistics") || uri.startsWith("/api/v1/mypage/statistics")) {
            return ErrorDomain.STATISTICS;
        }

        return ErrorDomain.GLOBAL;
    }
}