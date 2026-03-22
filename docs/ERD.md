## 1. member (회원)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 회원 고유 ID |
| `email` | `VARCHAR(255)` | 로그인 이메일 |
| `password_hash` | `VARCHAR(255)` | 비밀번호 해시 |
| `nickname` | `VARCHAR(50)` | 닉네임 |
| `role` | `VARCHAR(20)` | 권한 (`MEMBER`, `ADMIN`) |
| `desired_job` | `VARCHAR(100)` | 희망 직무 |
| `preferred_location` | `VARCHAR(100)` | 선호 근무 지역 |
| `subscription_plan` | `VARCHAR(20)` | 구독 등급 (`FREE`, `PRO`, `ENTERPRISE`) |
| `credit_balance` | `INT` | 크레딧 잔액 |
| `auth_provider` | `VARCHAR(20)` | 가입 경로 (`LOCAL`, `GOOGLE`, `KAKAO`) |
| `profile_image_url` | `VARCHAR(500)` | 프로필 이미지 URL |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |
| `deleted_at` | `TIMESTAMP` | 탈퇴 일시 |
| `status` | `VARCHAR(20)` | 유저 상태 (`ACTIVE`, `DORMANCY`, `DELETED`) |

---

## 2. social_auth (소셜 인증)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 소셜 인증 ID |
| `member_id` | `BIGINT` | 회원 ID |
| `provider_member_id` | `VARCHAR(255)` | 제공자 회원 고유 ID |
| `provider_type` | `VARCHAR(50)` | 제공자 타입 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |
| `deleted_at` | `TIMESTAMP` | 삭제 일시 |

---

## 3. resume (자기소개서)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 자기소개서 ID |
| `member_id` | `BIGINT` | 회원 ID |
| `title` | `VARCHAR(255)` | 자기소개서 제목 |
| `s3_file_url` | `VARCHAR(500)` | 자기소개서 파일 URL |
| `content` | `TEXT` | 추출된 자기소개서 본문 |
| `embedding` | `VECTOR(768)` | 임베딩 벡터 |
| `is_analyzed` | `BOOLEAN` | 분석 완료 여부 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 4. job_posting (채용 공고)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 채용 공고 ID |
| `member_id` | `BIGINT` | 등록 회원 ID |
| `company_name` | `VARCHAR(100)` | 회사명 |
| `job_title` | `VARCHAR(100)` | 직무명 |
| `posting_url` | `TEXT` | 공고 URL |
| `job_description` | `TEXT` | 공고 상세 내용 |
| `embedding` | `VECTOR(768)` | 임베딩 벡터 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 4-1. job_skill (채용 공고 스킬)

| 컬럼명 | 타입 | 설명       |
|---|---|----------|
| `id` | `BIGSERIAL` | 스킬 ID    |
| `job_posting_id` | `BIGINT` | 채용 공고 ID |
| `skill_name` | `VARCHAR(50)` | 스킬명      |
| `created_at` | `TIMESTAMP` | 생성 일시    |
| `updated_at` | `TIMESTAMP` | 수정 일시    |

---

## 5. analysis_report (분석 리포트)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 리포트 ID |
| `resume_id` | `BIGINT` | 자기소개서 ID |
| `job_posting_id` | `BIGINT` | 채용 공고 ID (일반 첨삭 시 NULL 가능)|
| `analysis_type` | `VARCHAR(20)` | 분석 유형 (`NORMAL`, `FIT_MATCH`) |
| `status` | `VARCHAR(20)` | 진행 상태 (`PENDING`, `PROCESSING`, `DELAYED`, `COMPLETED`, `FAILED`, `CANCELLED`) |
| `overall_feedback` | `TEXT` | 전체 피드백 |
| `sentence_corrections` | `JSONB` | 문장별 교정 내역 |
| `paragraph_summaries` | `JSONB` | 문단별 요약 |
| `revised_full_content` | `TEXT` | 전체 수정 제안본 |
| `job_description` | `TEXT` | 공고 설명 원문 |
| `job_description_text` | `TEXT` | 공고 설명 텍스트 |
| `match_score` | `INT` | 공고 매칭 점수 |
| `matching_feedback` | `TEXT` | 매칭 피드백 |
| `keyword_analysis` | `JSONB` | 키워드 분석 |
| `expected_questions` | `JSONB` | 예상 면접 질문 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 6. interview_session (면접 세션)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 면접 세션 ID |
| `member_id` | `BIGINT` | 회원 ID |
| `resume_id` | `BIGINT` | 자기소개서 ID |
| `job_posting_id` | `BIGINT` | 채용 공고 ID |
| `interview_mode` | `VARCHAR(20)` | 면접 모드 (`NORMAL`, `FOLLOW_UP`, `STRESS`) |
| `interview_type` | `VARCHAR(20)` | 면접 유형 (`TEXT`, `VOICE`) |
| `status` | `VARCHAR(20)` | 진행 상태 (`CREATED`, `IN_PROGRESS`, `DONE`, `ABORTED`) |
| `final_score` | `INT` | 최종 점수 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |
| `job_description` | `TEXT` | 면접 시 참고한 채용 공고/JD 내용 |
| `ai_provider` | `VARCHAR(50)` | 사용한 AI 제공자 |
| `model_variant` | `VARCHAR(100)` | 사용한 모델 버전/이름 |
| `overall_feedback` | `TEXT` | 면접 전체 종합 피드백 |
| `job_relevance_score` | `INT` | 직무 적합성 점수 |
| `attitude_confidence_score` | `INT` | 태도 및 자신감 점수 |
| `logical_structure_score` | `INT` | 답변 논리 구조 점수 |
| `clarity_score` | `INT` | 답변 명확성 점수 |
| `persuasiveness_score` | `INT` | 답변 설득력 점수 |
| `consistency_score` | `INT` | 답변 일관성 점수 |

---

## 7. interview_record (면접 상세 기록)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 기록 ID |
| `interview_session_id` | `BIGINT` | 면접 세션 ID |
| `question_text` | `TEXT` | 질문 내용 |
| `question_intent` | `TEXT` | 질문 의도 |
| `answer_text` | `TEXT` | 답변 내용 |
| `follow_up_depth` | `INT` | 꼬리질문 깊이 |
| `s3_file_url` | `VARCHAR(500)` | 음성 파일 URL |
| `wpm` | `INT` | 분당 발화 수 |
| `stt_accuracy` | `REAL` | STT 정확도 |
| `silence_count` | `INT` | 침묵 횟수 |
| `emotion_analysis` | `TEXT` | 감정 분석 결과 |
| `feedback_text` | `TEXT` | 피드백 내용 |
| `evaluation_score` | `REAL` | 답변 평가 점수 |
| `response_time_ms` | `INT` | 응답 시간(ms) |
| `turn_sequence` | `INT` | 대화 순서 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 7-1. interview_result_statistics (면접 결과 통계)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 통계 ID |
| `interview_session_id` | `BIGINT` | 면접 세션 ID (Unique) |
| `avg_clarity` | `DOUBLE PRECISION` | 평균 명확성 점수 |
| `avg_persuasiveness` | `DOUBLE PRECISION` | 평균 설득력 점수 |
| `avg_consistency` | `DOUBLE PRECISION` | 평균 일관성 점수 |
| `job_relevance_score` | `DOUBLE PRECISION` | 직무 적합성 점수 |
| `logical_structure_score` | `DOUBLE PRECISION` | 논리 구조 점수 |
| `attitude_confidence_score` | `DOUBLE PRECISION` | 태도/자신감 점수 |
| `overall_feedback` | `TEXT` | 종합 피드백 |
| `speech_habits` | `TEXT` | 말버릇 분석 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 8. question_scrap (질문 스크랩)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 스크랩 ID |
| `member_id` | `BIGINT` | 회원 ID |
| `interview_record_id` | `BIGINT` | 면접 기록 ID |
| `memo` | `TEXT` | 사용자 메모 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 9. notification (알림)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 알림 ID |
| `member_id` | `BIGINT` | 회원 ID |
| `message` | `VARCHAR(255)` | 알림 메시지 |
| `notification_type` | `VARCHAR(50)` | 알림 유형 |
| `is_read` | `BOOLEAN` | 읽음 여부 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 10. usage_log (사용량 로그)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 로그 ID |
| `member_id` | `BIGINT` | 회원 ID |
| `request_trace_id` | `VARCHAR(100)` | 요청 추적 ID |
| `service_type` | `VARCHAR(50)` | 서비스 유형 |
| `token_usage` | `INT` | 토큰 사용량 |
| `amount` | `INT` | 크레딧 변동량 |
| `balance_after` | `INT` | 변동 후 잔액 |
| `target_type` | `VARCHAR(50)` | 대상 타입 |
| `target_id` | `BIGINT` | 대상 ID |
| `description` | `VARCHAR(255)` | 상세 설명 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 11. audit_log (감사 로그)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 감사 로그 ID |
| `admin_id` | `BIGINT` | 관리자 회원 ID |
| `target_id` | `BIGINT` | 대상 ID |
| `target_type` | `VARCHAR(50)` | 대상 타입 |
| `action_type` | `VARCHAR(50)` | 수행 액션 |
| `action_detail` | `JSONB` | 상세 내용 |
| `ip_address` | `VARCHAR(50)` | 요청 IP |
| `member_agent` | `VARCHAR(255)` | User-Agent |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 12. attachment (첨부 파일)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 첨부 파일 ID |
| `owner_member_id` | `BIGINT` | 소유 회원 ID |
| `s3_key` | `VARCHAR(500)` | S3 저장 키 |
| `file_type` | `VARCHAR(30)` | 파일 유형 (`RESUME_ORIGINAL`, `RESUME_REVISED`, `INTERVIEW_AUDIO`) |
| `target_type` | `VARCHAR(50)` | 연결 대상 타입 |
| `target_id` | `BIGINT` | 연결 대상 ID |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 13. queue_job_metric (큐 작업 메트릭)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 메트릭 ID |
| `job_type` | `VARCHAR(50)` | 작업 유형 |
| `status` | `VARCHAR(30)` | 작업 상태 |
| `target_type` | `VARCHAR(50)` | 대상 타입 |
| `target_id` | `BIGINT` | 대상 ID |
| `message_id` | `VARCHAR(100)` | 큐 메시지 ID |
| `retry_count` | `INT` | 재시도 횟수 |
| `error_message` | `VARCHAR(1000)` | 에러 메시지 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 14. operation_metric_daily (일별 운영 지표)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 지표 ID |
| `metric_date` | `DATE` | 집계 날짜 |
| `service_type` | `VARCHAR(50)` | 서비스 유형 |
| `total_log_count` | `BIGINT` | 총 로그 수 |
| `total_token_usage` | `BIGINT` | 총 토큰 사용량 |
| `queue_enqueued_count` | `BIGINT` | 큐 적재 수 |
| `queue_success_count` | `BIGINT` | 큐 성공 수 |
| `queue_failed_count` | `BIGINT` | 큐 실패 수 |
| `error_count` | `BIGINT` | 에러 수 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 15. operation_metric_hourly (시간별 운영 지표)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 지표 ID |
| `metric_date` | `DATE` | 집계 날짜 |
| `metric_hour` | `INT` | 집계 시간(시) |
| `service_type` | `VARCHAR(50)` | 서비스 유형 |
| `total_log_count` | `BIGINT` | 총 로그 수 |
| `total_token_usage` | `BIGINT` | 총 토큰 사용량 |
| `queue_enqueued_count` | `BIGINT` | 큐 적재 수 |
| `queue_success_count` | `BIGINT` | 큐 성공 수 |
| `queue_failed_count` | `BIGINT` | 큐 실패 수 |
| `error_count` | `BIGINT` | 에러 수 |
| `failure_rate` | `DOUBLE PRECISION` | 실패율 |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 16. error_issue (에러 이슈)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 이슈 ID |
| `fingerprint` | `VARCHAR(255)` | 에러 그룹 식별값 |
| `title` | `VARCHAR(255)` | 이슈 제목 |
| `error_code` | `VARCHAR(100)` | 에러 코드 |
| `severity` | `VARCHAR(30)` | 심각도 (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) |
| `status` | `VARCHAR(30)` | 상태 (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `IGNORED`) |
| `error_domain` | `VARCHAR(50)` | 에러 도메인 (`GLOBAL`, `AUTH`, `RESUME`, `INTERVIEW`, `JOB_POSTING`, `FILE`, `STATISTICS`, `ADMIN`, `QUEUE`, `EXTERNAL_API`, `SYSTEM`) |
| `occurrence_count` | `BIGINT` | 발생 횟수 |
| `first_occurred_at` | `TIMESTAMP` | 최초 발생 시각 |
| `last_occurred_at` | `TIMESTAMP` | 마지막 발생 시각 |
| `last_error_log_id` | `BIGINT` | 마지막 에러 로그 ID |
| `created_at` | `TIMESTAMP` | 생성 일시 |
| `updated_at` | `TIMESTAMP` | 수정 일시 |

---

## 17. error_log (에러 로그)

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| `id` | `BIGSERIAL` | 에러 로그 ID |
| `issue_id` | `BIGINT` | 연결된 이슈 ID |
| `member_id` | `BIGINT` | 회원 ID |
| `error_code` | `VARCHAR(100)` | 에러 코드 |
| `exception_type` | `VARCHAR(255)` | 예외 타입 |
| `message` | `VARCHAR(1000)` | 에러 메시지 |
| `normalized_message` | `VARCHAR(1000)` | 정규화된 메시지 |
| `fingerprint` | `VARCHAR(255)` | 그룹핑 식별값 |
| `severity` | `VARCHAR(30)` | 심각도 (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) |
| `error_domain` | `VARCHAR(50)` | 에러 도메인 (`GLOBAL`, `AUTH`, `RESUME`, `INTERVIEW`, `JOB_POSTING`, `FILE`, `STATISTICS`, `ADMIN`, `QUEUE`, `EXTERNAL_API`, `SYSTEM`) |
| `request_trace_id` | `VARCHAR(100)` | 요청 추적 ID |
| `target_type` | `VARCHAR(100)` | 대상 타입 |
| `target_id` | `BIGINT` | 대상 ID |
| `stack_trace` | `TEXT` | 스택 트레이스 |
| `occurred_at` | `TIMESTAMP` | 실제 발생 시각 |
| `created_at` | `TIMESTAMP` | 저장 시각 |

---

# 테이블 관계

- `member` 1 : N `social_auth`
- `member` 1 : N `resume`
- `member` 1 : N `job_posting`
- `job_posting` 1 : N `job_skill`
- `resume` 1 : N `analysis_report`
- `job_posting` 1 : N `analysis_report`
- `member` 1 : N `interview_session`
- `interview_session` 1 : N `interview_record`
- `interview_session` 1 : 1 `interview_result_statistics`
- `member` 1 : N `question_scrap`
- `interview_record` 1 : N `question_scrap`
- `member` 1 : N `notification`
- `member` 1 : N `usage_log`
- `member` 1 : N `audit_log`
- `member` 1 : N `attachment`
- `error_issue` 1 : N `error_log`

---

# 인덱스 및 제약조건 요약

## Unique
- `member.email`
- `social_auth(provider_type, provider_member_id)`
- `analysis_report(resume_id, job_posting_id)`
- `interview_result_statistics.interview_session_id`
- `operation_metric_daily(metric_date, service_type)`
- `operation_metric_hourly(metric_date, metric_hour, service_type)`
- `error_issue.fingerprint`

## Index
- `idx_skill_name`
- `idx_interview_result_statistics_created_at`
- `idx_usage_member`
- `idx_usage_created`
- `idx_usage_service`
- `idx_attachment_owner`
- `idx_attachment_target`
- `idx_queue_job_target`
- `idx_queue_job_status`
- `idx_queue_job_created_at`
- `idx_operation_metric_hourly_date_hour`
- `idx_operation_metric_hourly_date_service`
- `idx_error_issue_fingerprint`
- `idx_error_issue_status`
- `idx_error_issue_severity`
- `idx_error_issue_last_occurred_at`
- `idx_error_log_issue`
- `idx_error_log_fingerprint`
- `idx_error_log_occurred_at`
- `idx_error_log_error_code`