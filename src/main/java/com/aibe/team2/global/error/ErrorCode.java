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
    COMMON_402(HttpStatus.BAD_REQUEST, "COMMON_402", "잘못된 JSON 형식입니다."),
    COMMON_403(HttpStatus.FORBIDDEN, "COMMON_403", "접근 권한이 없습니다."),
    COMMON_404(HttpStatus.NOT_FOUND, "COMMON_404", "리소스를 찾을 수 없습니다."),
    COMMON_405(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "지원하지 않는 HTTP 메서드입니다."),
    COMMON_406(HttpStatus.BAD_REQUEST, "COMMON_406", "입력 값의 타입이 일치하지 않습니다."),
    COMMON_407(HttpStatus.BAD_REQUEST, "COMMON_407", "필수 파라미터가 누락되었습니다."),
    COMMON_408(HttpStatus.BAD_REQUEST, "COMMON_408", "유효하지 않은 파라미터입니다."),
    COMMON_409(HttpStatus.CONFLICT, "COMMON_409", "요청이 현재 상태와 충돌합니다."),
    COMMON_429(HttpStatus.TOO_MANY_REQUESTS, "COMMON_429", "너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요."),
    COMMON_500(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다."),
    COMMON_JSON_CONVERSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_501", "JSON 데이터 변환 중 오류가 발생했습니다."),

    // Auth(인증)
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_001", "유효하지 않은 토큰입니다."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰이 만료되었습니다."),
    AUTH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_003", "Refresh Token이 없습니다."),
    AUTH_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_004", "이메일 또는 비밀번호가 일치하지 않습니다."),
    AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_005", "접근 권한이 없습니다."),
    AUTH_SOCIAL_LOGIN_FAILED(HttpStatus.BAD_REQUEST, "AUTH_006", "소셜 로그인에 실패했습니다."),
    AUTH_DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "AUTH_007", "이미 사용중인 닉네임입니다."),
    AUTH_DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_008", "이미 사용중인 이메일입니다."),
    AUTH_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_009", "이메일이 입력되지 않았습니다."),
    AUTH_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "AUTH_010", "인증 번호가 일치하지 않거나 만료되었습니다."),

    // User (회원)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),
    USER_EMAIL_DUPLICATION(HttpStatus.CONFLICT, "USER_002", "이미 존재하는 이메일입니다."),
    USER_NICKNAME_DUPLICATION(HttpStatus.CONFLICT, "USER_003", "이미 존재하는 닉네임입니다."),
    USER_PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "USER_004", "비밀번호 형식이 올바르지 않습니다."),
    USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "USER_005", "탈퇴 처리된 회원입니다."),
    USER_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "USER_006", "비밀번호가 일치하지 않습니다."),

    // File (파일)
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "FILE_001", "파일이 비어있습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "FILE_002", "파일 크기가 제한을 초과했습니다."),
    FILE_EXTENSION_INVALID(HttpStatus.BAD_REQUEST, "FILE_003", "지원하지 않는 파일 형식입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_004", "파일 업로드 중 오류가 발생했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_005", "파일을 찾을 수 없습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_006", "파일 삭제에 실패했습니다."),

    // Job Posting (채용 공고) - 현재 아예 누락되어 있음
    JOB_POSTING_NOT_FOUND(HttpStatus.NOT_FOUND, "JOB_001", "채용 공고를 찾을 수 없습니다."),
    JOB_POSTING_CRAWL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JOB_002", "채용 공고 URL 크롤링에 실패했습니다."),
    JOB_POSTING_OWNERSHIP_ERROR(HttpStatus.FORBIDDEN, "JOB_003", "해당 채용 공고에 대한 접근 권한이 없습니다."),

    // Resume (자기소개서)
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "RESUME_001", "해당 자기소개서를 찾을 수 없습니다."),
    RESUME_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "RESUME_002", "자기소개서 내용을 분석하는 중 오류가 발생했습니다."),
    RESUME_OWNERSHIP_ERROR(HttpStatus.FORBIDDEN, "RESUME_003", "해당 자기소개서에 대한 접근 권한이 없습니다."),

    // Resume Analysis (자기소개서 첨삭/분석 리포트)
    ANALYSIS_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYSIS_001", "자기소개서 분석 리포트를 찾을 수 없습니다."),
    ANALYSIS_IN_PROGRESS(HttpStatus.CONFLICT, "ANALYSIS_002", "현재 자기소개서 분석이 진행 중입니다. 잠시 후 다시 시도해주세요."),
    ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ANALYSIS_003", "자기소개서 분석에 실패했습니다."),

    // Interview (면접)
    INTERVIEW_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "INTERVIEW_001", "면접 세션을 찾을 수 없습니다."),
    INTERVIEW_ALREADY_FINISHED(HttpStatus.BAD_REQUEST, "INTERVIEW_002", "이미 종료된 면접입니다."),
    INTERVIEW_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "INTERVIEW_003", "질문 정보를 찾을 수 없습니다."),
    INTERVIEW_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "INTERVIEW_004", "면접 상세 기록(턴)을 찾을 수 없습니다."),
    INTERVIEW_OWNERSHIP_ERROR(HttpStatus.FORBIDDEN, "INTERVIEW_005", "해당 면접 기록에 대한 접근 권한이 없습니다."),
    INTERVIEW_NOT_COMPLETED(HttpStatus.CONFLICT, "INTERVIEW_006", "면접이 완료되지 않아 파일을 다운로드할 수 없습니다."),

    // Scrap (스크랩)
    SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND, "SCRAP_001", "스크랩한 질문을 찾을 수 없습니다."),
    SCRAP_ALREADY_EXISTS(HttpStatus.CONFLICT, "SCRAP_002", "이미 스크랩된 질문입니다."),

    // Subscription (구독 및 멤버십)
    SUB_NOT_SUBSCRIBED(HttpStatus.FORBIDDEN, "SUB_001", "프리미엄 구독 요금제 가입이 필요한 서비스입니다."),
    SUB_EXPIRED(HttpStatus.FORBIDDEN, "SUB_002", "구독 기간이 만료되었습니다. 요금제를 연장해주세요."),
    SUB_PAYMENT_REQUIRED(HttpStatus.PAYMENT_REQUIRED, "SUB_003", "결제가 완료되지 않아 서비스를 이용할 수 없습니다."),
    SUB_FEATURE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "SUB_004", "현재 이용 중인 요금제에서는 지원하지 않는 기능입니다."),
    SUBSCRIPTION_EXPIRED(HttpStatus.FORBIDDEN, "SUB_005", "구독 기간이 만료되었습니다."),
    FEATURE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "SUB_006", "현재 구독 요금제에서는 지원하지 않는 기능입니다."),

    // Credits (사용량/잔여 크레딧)
    CREDIT_INSUFFICIENT(HttpStatus.FORBIDDEN, "CREDIT_001", "잔여 크레딧(이용권)이 부족합니다. 크레딧을 충전해주세요."),
    CREDIT_DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "CREDIT_002", "일일 기본 제공 무료 분석 횟수를 모두 소진했습니다."),
    CREDIT_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CREDIT_003", "크레딧 사용 내역을 찾을 수 없습니다."),

    // Notification(알림)
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI_001", "존재하지 않는 알림입니다."),

    // External API (외부 연동)
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_001", "AI 서비스 연결이 지연되고 있습니다. 잠시 후 다시 시도해주세요."),
    AI_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AI_002", "AI API 요청 할당량을 초과했습니다. 관리자에게 문의해주세요."),
    AI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI_003", "AI 서비스 응답 시간이 초과되었습니다."),
    AI_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "AI_004", "AI 서비스에 잘못된 요청(프롬프트 등)이 전달되었습니다."),
    AI_CONTENT_FILTERED(HttpStatus.BAD_REQUEST, "AI_005", "안전 정책(Safety Policy)에 의해 AI가 답변 생성을 거부했습니다."),
    AI_RESPONSE_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI_006", "AI가 생성한 응답 데이터(JSON)를 분석하는 데 실패했습니다."),
    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI_007", "외부 AI 서버 내부에서 일시적인 오류가 발생했습니다."),

    // Database (DB)
    DB_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB_001", "데이터베이스 연결에 실패했습니다."),
    DB_DEADLOCK_ERROR(HttpStatus.CONFLICT, "DB_002", "데이터베이스 트랜잭션 충돌(데드락)이 발생했습니다. 잠시 후 다시 시도해주세요."),
    DB_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST, "DB_003", "데이터 무결성 제약 조건(중복 키, 필수 값 누락 등)을 위반했습니다."),
    DB_DATA_ACCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB_004", "데이터베이스 접근 및 SQL 쿼리 실행 중 오류가 발생했습니다."),

    // System & Infrastructure (시스템 및 인프라)
    SYS_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "시스템 내부에서 예기치 못한 오류가 발생했습니다."),
    SYS_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "SYS_002", "시스템 처리 시간이 초과되었습니다."),
    SYS_ASYNC_PROCESS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_003", "비동기 백그라운드 작업 처리 중 오류가 발생했습니다."),
    SYS_RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "SYS_004", "시스템 리소스를 찾을 수 없습니다."),

    // Redis (캐시 및 메시지 큐)
    REDIS_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_001", "캐시 서버(Redis) 연결에 실패했습니다."),
    REDIS_OPERATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_002", "캐시 서버(Redis) 데이터 읽기/쓰기 작업 중 오류가 발생했습니다."),
    REDIS_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "REDIS_003", "캐시 서버(Redis) 응답 시간이 초과되었습니다."),
    REDIS_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_004", "캐시 서버(Redis) 데이터를 직렬화/역직렬화하는 중 오류가 발생했습니다."),

    // Error Issue (에러 이슈)
    ERROR_ISSUE_NOT_FOUND(HttpStatus.NOT_FOUND, "ERROR_001", "에러 이슈를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}