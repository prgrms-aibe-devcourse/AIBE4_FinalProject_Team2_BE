package com.aibe.team2.global.error;

import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.service.ErrorLogService;
import com.aibe.team2.domain.error.util.ErrorDomainResolver;
import com.aibe.team2.global.common.response.ErrorResponse;
import com.aibe.team2.global.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    // [Custom] 비즈니스 로직 예외 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);

        errorLogService.record(
                errorCode.getCode(),
                errorDomain,
                e,
                null,
                request.getRequestURI(),
                null
        );

        log.error("BusinessException: code={}, message={}",
                errorCode.getCode(), e.getMessage());

        return ErrorResponse.toResponseEntity(errorCode);
    }

    // [Validation] DTO 바인딩 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);

        errorLogService.record(
                ErrorCode.COMMON_400.getCode(),
                errorDomain,
                e,
                null,
                request.getRequestURI(),
                null
        );

        log.error("Validation Error: {}",
                e.getBindingResult().getFieldError() != null
                        ? e.getBindingResult().getFieldError().getDefaultMessage()
                        : e.getMessage());

        return ErrorResponse.toResponseEntity(ErrorCode.COMMON_400, e.getBindingResult());
    }

    // [File] 파일 용량 초과
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ErrorResponse> handleFileSizeException(
            MaxUploadSizeExceededException e,
            HttpServletRequest request
    ) {
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);

        errorLogService.record(
                ErrorCode.FILE_SIZE_EXCEEDED.getCode(),
                errorDomain,
                e,
                null,
                request.getRequestURI(),
                null
        );

        log.error("File Size Exceeded: {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    // [Spring] JSON 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request
    ) {
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);

        errorLogService.record(
                ErrorCode.COMMON_400.getCode(),
                errorDomain,
                e,
                null,
                request.getRequestURI(),
                null
        );

        log.error("HttpMessageNotReadableException: {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.COMMON_400);
    }

    // [Spring] 파라미터 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request
    ) {
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);

        errorLogService.record(
                ErrorCode.COMMON_406.getCode(),
                errorDomain,
                e,
                null,
                request.getRequestURI(),
                null
        );

        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.COMMON_406);
    }

    // [System] 예상 못한 모든 예외
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        ErrorDomain errorDomain = errorDomainResolver.resolve(request);

        errorLogService.record(
                ErrorCode.COMMON_500.getCode(),
                errorDomain,
                e,
                null,
                request.getRequestURI(),
                null
        );

        log.error("Internal Server Error", e);
        return ErrorResponse.toResponseEntity(ErrorCode.COMMON_500);
    }
}