package com.aibe.team2.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {


    // Common (공통)
    COMMON_400(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    COMMON_401(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    COMMON_403(HttpStatus.FORBIDDEN, "COMMON_403", "접근 권한이 없습니다."),
    COMMON_404(HttpStatus.NOT_FOUND, "COMMON_404", "리소스를 찾을 수 없습니다."),
    COMMON_405(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "지원하지 않는 HTTP 메서드입니다."),
    COMMON_409(HttpStatus.CONFLICT, "COMMON_409", "요청이 현재 상태와 충돌합니다."),
    COMMON_500(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다."),

    // Auth(인증)
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_001", "유효하지 않은 토큰입니다."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰이 만료되었습니다."),
    AUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_003", "이메일 또는 비밀번호가 일치하지 않습니다."),
    AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_004", "접근 권한이 없습니다."),
    AUTH_SOCIAL_LOGIN_FAILED(HttpStatus.BAD_REQUEST, "AUTH_005", "소셜 로그인에 실패했습니다."),

    // User (회원)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),
    USER_EMAIL_DUPLICATION(HttpStatus.CONFLICT, "USER_002", "이미 존재하는 이메일입니다."),
    USER_NICKNAME_DUPLICATION(HttpStatus.CONFLICT, "USER_003", "이미 존재하는 닉네임입니다."),
    USER_PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "USER_004", "비밀번호 형식이 올바르지 않습니다."),

    // File (파일)
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "FILE_001", "파일이 비어있습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_002", "파일 크기가 제한을 초과했습니다."),
    FILE_EXTENSION_INVALID(HttpStatus.BAD_REQUEST, "FILE_003", "지원하지 않는 파일 형식입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_004", "파일 업로드 중 오류가 발생했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_005", "파일을 찾을 수 없습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_006", "파일 삭제에 실패했습니다."),

    // Resume (자소서)
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "RESUME_001", "해당 이력서를 찾을 수 없습니다."),
    RESUME_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RESUME_002", "이력서 내용을 분석하는 중 오류가 발생했습니다."),
    RESUME_OWNERSHIP_ERROR(HttpStatus.FORBIDDEN, "RESUME_003", "해당 이력서에 대한 접근 권한이 없습니다."),

    // Interview (면접)
    INTERVIEW_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "INTERVIEW_001", "면접 세션을 찾을 수 없습니다."),
    INTERVIEW_ALREADY_FINISHED(HttpStatus.BAD_REQUEST, "INTERVIEW_002", "이미 종료된 면접입니다."),
    INTERVIEW_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "INTERVIEW_003", "질문 정보를 찾을 수 없습니다."),

    // Credits (사용량/잔여 크레딧)
    CREDIT_INSUFFICIENT(HttpStatus.FORBIDDEN, "CREDIT_001", "잔여 크레딧이 부족합니다."),
    CREDIT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CREDIT_002", "크레딧 사용 내역을 찾을 수 없습니다."),

    // External API (외부 연동)
    EXTERNAL_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_001", "외부 AI 서비스 응답이 지연되고 있습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}