package com.aibe.team2.global.error;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.service.ErrorLogService;
import com.aibe.team2.domain.error.util.ErrorDomainResolver;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.global.common.response.ErrorResponse;
import com.aibe.team2.global.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorLogService errorLogService;
    private final ErrorDomainResolver errorDomainResolver;

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);
        Member currentMember = getCurrentMember();

        errorLogService.record(
                errorCode.getCode(),
                errorDomain,
                e,
                currentMember,
                request.getRequestURI(),
                null
        );

        log.error("BusinessException: code={}, message={}",
                errorCode.getCode(), e.getMessage());

        return ErrorResponse.toResponseEntity(errorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);
        Member currentMember = getCurrentMember();

        errorLogService.record(
                ErrorCode.COMMON_400.getCode(),
                errorDomain,
                e,
                currentMember,
                request.getRequestURI(),
                null
        );

        log.error("Validation Error: {}",
                e.getBindingResult().getFieldError() != null
                        ? e.getBindingResult().getFieldError().getDefaultMessage()
                        : e.getMessage());

        return ErrorResponse.toResponseEntity(ErrorCode.COMMON_400, e.getBindingResult());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ErrorResponse> handleFileSizeException(
            MaxUploadSizeExceededException e,
            HttpServletRequest request
    ) {
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);
        Member currentMember = getCurrentMember();

        errorLogService.record(
                ErrorCode.FILE_SIZE_EXCEEDED.getCode(),
                errorDomain,
                e,
                currentMember,
                request.getRequestURI(),
                null
        );

        log.error("File Size Exceeded: {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request
    ) {
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);
        Member currentMember = getCurrentMember();

        errorLogService.record(
                ErrorCode.COMMON_400.getCode(),
                errorDomain,
                e,
                currentMember,
                request.getRequestURI(),
                null
        );

        log.error("HttpMessageNotReadableException: {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.COMMON_400);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request
    ) {
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);
        Member currentMember = getCurrentMember();

        errorLogService.record(
                ErrorCode.COMMON_406.getCode(),
                errorDomain,
                e,
                currentMember,
                request.getRequestURI(),
                null
        );

        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.COMMON_406);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);
        Member currentMember = getCurrentMember();

        errorLogService.record(
                ErrorCode.COMMON_500.getCode(),
                errorDomain,
                e,
                currentMember,
                request.getRequestURI(),
                null
        );

        log.error("Internal Server Error", e);
        return ErrorResponse.toResponseEntity(ErrorCode.COMMON_500);
    }

    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getMember();
        }

        return null;
    }
}
