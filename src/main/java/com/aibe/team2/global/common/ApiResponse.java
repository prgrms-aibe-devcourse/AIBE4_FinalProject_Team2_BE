package com.aibe.team2.global.common;

import lombok.Builder;
import lombok.Getter;

/**
 * 아래의 코드는 전부 예시입니다. 추후 기능이나 필요에 맞게 자유롭게 수정하면 됩니다.
 */
@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    @Builder
    private ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 성공 응답 정적 팩토리 메서드
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("OK")
                .message("요청 성공")
                .data(data)
                .build();
    }

    // TODO: ErrorCode Enum과 연동하여 실패(success: false) 응답 메서드 구현 필요
}