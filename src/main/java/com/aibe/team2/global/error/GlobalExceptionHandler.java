package com.aibe.team2.global.error;

import com.aibe.team2.global.common.response.ErrorResponse;
import com.aibe.team2.global.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //[Custom] 비즈니스 로직 예외 처리
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException: code={}, message={}",
                e.getErrorCode().getCode(), e.getMessage());
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }
    // [Validation] @Valid, @Validated 바인딩 에러 처리
    // (회원가입, 로그인 등 DTO 검증 실패 시)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation Error: {}", e.getBindingResult().getFieldError().getDefaultMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.COMMON_400, e.getBindingResult());
    }
    // [File] 파일 용량 초과 에러 처리
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ErrorResponse> handleFileSizeException(MaxUploadSizeExceededException e) {
        log.error("File Size Exceeded: {}", e.getMessage());
        return ErrorResponse.toResponseEntity(ErrorCode.FILE_SIZE_EXCEEDED);
    }
    //[System] 그 외 알 수 없는 시스템 예외
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Internal Server Error: ", e);
        return ErrorResponse.toResponseEntity(ErrorCode.COMMON_500);
    }
}