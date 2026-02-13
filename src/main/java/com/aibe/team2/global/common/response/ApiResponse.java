package com.aibe.team2.global.common.response;

import lombok.Builder;
import lombok.Getter;

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

    // 데이터가 있는 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("OK")
                .message("요청 성공")
                .data(data)
                .build();
    }

    // 데이터가 없는 성공 응답
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .code("OK")
                .message("요청 성공")
                .build();
    }
    // 메시지 커스텀
    public static <T> ApiResponse<T> success(String customMessage) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("OK")
                .message(
                        customMessage == null ? "요청 성공" : customMessage)
                .build();

    }
}