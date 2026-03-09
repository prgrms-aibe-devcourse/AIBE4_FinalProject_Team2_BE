### 1. member (회원)

| **컬럼명** | **타입** | **설명** |
| :--- | :--- | :--- |
| `id` | `BIGINT` | 회원 고유 ID |
| `email` | `VARCHAR(255)` | 로그인 이메일 |
| `password_hash` | `VARCHAR(255)` | 비밀번호 (Hash) |
| `nickname` | `VARCHAR(50)` | 닉네임 |
| `role` | `ENUM('MEMBER', 'ADMIN')` | 권한 |
| `desired_job` | `VARCHAR(100)` | 희망 직무 |
| `preferred_location` | `VARCHAR(100)` | 선호 근무 지역 |
| `subscription_plan` | `ENUM('FREE', 'PRO', 'ENTERPRISE')` | 구독 등급 |
| `credit_balance` | `INT` | 크레딧 잔액 (자산) |
| `auth_provider` | `ENUM('LOCAL', 'GOOGLE', 'KAKAO')` | 가입 경로 (인증 제공자) |
| `profile_image_url` | `VARCHAR(500)` | 프로필 이미지 URL |
| `created_at` | `DATETIME` | 가입 일시 |
| `updated_at` | `DATETIME` | 수정 일시 |
| `deleted_at` | `DATETIME` | 탈퇴 일시 (Soft Delete) |

### 2. social_auth (소셜 인증)

| **컬럼명** | **타입** | **설명** |
| :--- | :--- | :--- |
| `id` | `BIGINT` | 소셜 인증 ID |
| `member_id` | `BIGINT` | 회원 ID |
| `provider_MEMBER_id` | `VARCHAR(255)` | 제공자 측 유저 ID |
| `provider_type` | `VARCHAR(50)` | 제공자 타입 (Google, Kakao 등) |
| `created_at` | `DATETIME` | 연동 일시 |
| `updated_at` | `DATETIME` | 수정 일시 |
| `deleted_at` | `DATETIME` | 탈퇴 일시 (Soft Delete) |

### 3. resume (자기소개서)

| **컬럼명** | **타입** | **설명**          |
| :--- | :--- |:----------------|
| `id` | `BIGINT` | 자기소개서 ID        |
| `member_id` | `BIGINT` | 회원 ID           |
| `title` | `VARCHAR(255)` | 자기소개서 제목        |
| `s3_file_url` | `VARCHAR(500)` | 자기소개서 파일 URL    |
| `content` | `TEXT` | 자기소개서 텍스트 추출 내용 |
| `is_analyzed` | `BOOLEAN` | 분석 완료 여부        |
| `created_at` | `DATETIME` | 생성 일시           |
| `updated_at` | `DATETIME` | 수정 일시           |

### 4. job_posting (채용 공고)

| **컬럼명** | **타입** | **설명** |
| :--- | :--- | :--- |
| `id` | `BIGINT` | 채용 공고 ID |
| `member_id` | `BIGINT` | 회원 ID (등록자) |
| `company_name` | `VARCHAR(100)` | 회사명 |
| `job_title` | `VARCHAR(100)` | 직무명 (공고 제목) |
| `posting_url` | `TEXT` | 공고 URL |
| `job_description` | `TEXT` | 직무 상세 내용 (JD) |
| `created_at` | `DATETIME` | 생성 일시 |
| `updated_at` | `DATETIME` | 수정 일시 |

### 4-1. job_skill (채용 공고 스킬)

| **컬럼명** | **타입** | **설명** |
| :--- | :--- | :--- |
| `id` | `BIGINT` | 스킬 ID |
| `job_posting_id` | `BIGINT` | 채용 공고 ID |
| `skill_name` | `VARCHAR(50)` | 스킬명 (Java, Python 등) |
| `created_at` | `DATETIME` | 생성 일시 |
| `updated_at` | `DATETIME` | 수정 일시 |

### 5. analysis_report (분석 리포트)

| **컬럼명**                | **타입** | **설명**          |
|:-----------------------| :--- |:----------------|
| `id`                   | `BIGINT` | 리포트 ID          |
| `resume_id`            | `BIGINT` | 자기소개서 ID        |
| `job_posting_id`       | `BIGINT` | 채용 공고 ID        |
| `match_score`          | `INT` | 매칭 점수           |
| `keyword_analysis`     | `JSON` | 키워드 분석 데이터      |
| `sentence_correction`  | `JSON` | 문장 교정 데이터       |
| `generated_subtitle`   | `JSON` | 생성된 소제목         |
| `revised_full_content` | `TEXT` | 수정 제안된 자기소개서 내용 |
| `status`               | `ENUM(...)` | 진행 상태           |
| `created_at`           | `DATETIME` | 생성 일시           |
| `updated_at`           | `DATETIME` | 수정 일시           |

### 6. interview_session (면접 세션)

| **컬럼명** | **타입** | **설명**               |
| :--- | :--- |:---------------------|
| `id` | `BIGINT` | 세션 ID                |
| `member_id` | `BIGINT` | 회원 ID                |
| `resume_id` | `BIGINT` | 자기소개서 ID             |
| `job_posting_id` | `BIGINT` | 채용 공고 ID             |
| `interview_mode` | `ENUM(...)` | 면접 모드 (일반, 꼬리질문, 압박) |
| `interview_type` | `ENUM('TEXT', 'VOICE')` | 면접 방식 (텍스트, 음성)      |
| `status` | `ENUM('IN_PROGRESS', 'COMPLETED')` | 진행 상태                |
| `final_score` | `INT` | 최종 점수                |
| `created_at` | `DATETIME` | 시작 일시                |
| `updated_at` | `DATETIME` | 수정 일시                |

### 7. interview_record (면접 상세 기록)

| **컬럼명** | **타입** | **설명** |
| :--- | :--- | :--- |
| `id` | `BIGINT` | 기록 ID |
| `interview_session_id` | `BIGINT` | 면접 세션 ID |
| `turn_sequence` | `INT` | 대화 순서 (Turn) |
| `question_text` | `TEXT` | 질문 내용 |
| `question_intent` | `TEXT` | 질문 의도 |
| `answer_text` | `TEXT` | 답변 내용 |
| `follow_up_depth` | `INT` | 꼬리질문 깊이 |
| `s3_file_url` | `VARCHAR(500)` | 답변 음성 파일 URL |
| `wpm` | `INT` | 발화 속도 (Words Per Minute) |
| `stt_accuracy` | `FLOAT` | STT 정확도 |
| `silence_count` | `INT` | 침묵 횟수 |
| `emotion_analysis` | `JSON` | 감정 분석 결과 |
| `feedback_text` | `TEXT` | 피드백 내용 |
| `evaluation_score` | `FLOAT` | 답변 평가 점수 |
| `response_time_ms` | `INT` | 응답 소요 시간 (ms) |
| `created_at` | `DATETIME` | 생성 일시 |
| `updated_at` | `DATETIME` | 수정 일시 |
### 8. question_scrap (질문 스크랩)

| **컬럼명** | **타입** | **설명** |
| :--- | :--- | :--- |
| `id` | `BIGINT` | 스크랩 ID |
| `member_id` | `BIGINT` | 회원 ID |
| `interview_record_id` | `BIGINT` | 면접 기록(질문) ID |
| `memo` | `TEXT` | 사용자 메모 |
| `created_at` | `DATETIME` | 스크랩 일시 |
| `updated_at` | `DATETIME` | 수정 일시 |

### 9. notification (알림)

| **컬럼명** | **타입** | **설명** |
| :--- | :--- | :--- |
| `id` | `BIGINT` | 알림 ID |
| `member_id` | `BIGINT` | 회원 ID |
| `message` | `VARCHAR(255)` | 알림 메시지 |
| `notification_type` | `VARCHAR(50)` | 알림 유형 |
| `is_read` | `BOOLEAN` | 읽음 여부 |
| `created_at` | `DATETIME` | 생성 일시 |
| `updated_at` | `DATETIME` | 수정 일시 |

### 10. usage_log (사용량 로그)

| **컬럼명** | **타입** | **설명** |
| :--- | :--- | :--- |
| `id` | `BIGINT` | 로그 ID |
| `member_id` | `BIGINT` | 회원 ID |
| `request_trace_id` | `VARCHAR(100)` | 요청 추적 ID |
| `service_type` | `VARCHAR(50)` | 서비스 유형 |
| `token_usage` | `INT` | 토큰 사용량 |
| `amount` | `INT` | 변동량 (+/-) |
| `balance_after` | `INT` | 변동 후 잔액 |
| `target_id` | `BIGINT` | 대상 ID (리포트/세션 등) |
| `target_type` | `VARCHAR(50)` | 대상 타입 |
| `description` | `VARCHAR(255)` | 상세 내역 |
| `created_at` | `DATETIME` | 발생 일시 |
| `updated_at` | `DATETIME` | 수정 일시 |

### 11. audit_log (감사 로그)

| **컬럼명** | **타입** | **설명** |
| :--- | :--- | :--- |
| `id` | `BIGINT` | 감사 로그 ID |
| `admin_id` | `BIGINT` | 관리자(회원) ID |
| `target_id` | `BIGINT` | 대상 ID |
| `target_type` | `VARCHAR(50)` | 대상 타입 |
| `action_type` | `VARCHAR(50)` | 수행 동작 |
| `action_detail` | `JSON` | 상세 내용 (JSON) |
| `ip_address` | `VARCHAR(50)` | 접속 IP |
| `member_agent` | `VARCHAR(255)` | 유저 에이전트 |
| `created_at` | `DATETIME` | 발생 일시 |
| `updated_at` | `DATETIME` | 수정 일시 |

### 12. attachment (첨부 파일)
| **컬럼명** | **타입** | **설명**              |
| :--- | :--- |:--------------------|
| `id` | `BIGINT` | 파일 ID               |
| `owner_member_id` | `BIGINT` | 소유자(회원) ID          |
| `s3_key` | `VARCHAR(500)` | 저장소 Key (경로)        |
| `file_type` | `ENUM('RESUME_ORIGINAL','RESUME_REVISED','INTERVIEW_AUDIO')` | 파일 유형 (자기소개서/오디오 등) |
| `target_type` | `VARCHAR(50)` | 연결 대상 타입            |
| `target_id` | `BIGINT` | 연결 대상 ID            |
| `created_at` | `DATETIME` | 업로드 일시              |
| `updated_at` | `DATETIME` | 수정 일시               |
