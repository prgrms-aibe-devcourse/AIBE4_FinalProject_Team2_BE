package com.aibe.team2.global.common.response;

import com.aibe.team2.global.error.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API 에러 응답 공통 포맷
 */
@Getter
public class ErrorResponse {
    private final boolean success = false;
    private final String code;
    private final String message;
    private final List<ValidationError> errors;

    @Builder
    private ErrorResponse(String code, String message, List<ValidationError> errors) {
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    // 1. 일반 에러 응답 (커스텀 예외용)
    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    // 2. Validation 에러 응답 (@Valid 실패용)
    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode, BindingResult bindingResult) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .errors(ValidationError.of(bindingResult))
                        .build());
    }

    // 3. 메시지 커스텀 응답 (선택적)
    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode, String customMessage) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .code(errorCode.getCode())
                        .message(customMessage)
                        .build());
    }

    // Validation 상세 에러 정보
    @Getter
    @Builder
    public static class ValidationError {
        private final String field;
        private final String reason;

        public static List<ValidationError> of(BindingResult bindingResult) {
            if (bindingResult == null) return Collections.emptyList();

            return bindingResult.getFieldErrors().stream()
                    .map(error -> ValidationError.builder()
                            .field(error.getField())
                            .reason(error.getDefaultMessage())
                            .build())
                    .collect(Collectors.toList());
        }
    }
}