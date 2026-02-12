# ERD (Entity Relationship Diagram)

### 1. user (회원 & 지갑)
| **컬럼명** | **타입** | **제약조건** | **설명** |
| -- | --- | --- | --- |
| id | `BIGINT` | **PK, AUTO_INCREMENT | 사용자 고유 ID |
| email | `VARCHAR(255)` | UNIQUE, NOT NULL | 로그인 이메일 |
| password | `VARCHAR(255)` | NULLABLE | 비밀번호 (소셜 로그인 시 NULL) |
| nickname | `VARCHAR(50)` | NOT NULL | 닉네임 |
| role | `ENUM` | DEFAULT 'USER' | 권한 (USER, ADMIN) |
| subscription_plan | `ENUM` | DEFAULT 'FREE' | 구독 등급 (FREE, PRO, ENTERPRISE) |
| remaining_credits | `INT` | DEFAULT 0 | 잔여 크레딧 (지갑 잔액) |
| desired_job_role | `VARCHAR(100)` | NULLABLE | 희망 직무 (프로필 설정) |
| preferred_location | `VARCHAR(100)` | NULLABLE | 선호 근무 지역 |
| provider | `ENUM` | DEFAULT 'LOCAL' | 가입 경로 (LOCAL, GOOGLE, KAKAO) |
| profile_image_url | `VARCHAR(500)` | NULLABLE | 프로필 이미지 URL |
| created_at | `DATETIME` | DEFAULT NOW() | 가입 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |
| deleted_at | `DATETIME` | NULLABLE | 탈퇴 일시 (Soft Delete) |

### 2. social_auth (소셜 연동)
| **컬럼명** | **타입** | **제약조건** | **설명** |
| -- | --- | --- | --- |
| id | `BIGINT` | PK, AUTO_INCREMENT | 고유 ID |
| user_id | `BIGINT` | FK (user.id), CASCADE | 사용자 ID |
| provider_id | `VARCHAR(255)` | NOT NULL | 소셜 서비스 측 고유 ID (sub 등) |
| provider_type | `VARCHAR(50)` | NOT NULL | 소셜 타입 (GOOGLE, KAKAO) |
| created_at | `DATETIME` | DEFAULT NOW() | 연동 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |
### 3. resume (이력서)
| **컬럼명** | **타입** | **제약조건** | **설명** |
| -- | --- | --- | --- |
| id | `BIGINT` | PK, AUTO_INCREMENT | 이력서 ID |
| user_id | `BIGINT` | FK (user.id) | 소유자 ID |
| title | `VARCHAR(255)` | NOT NULL | 이력서 제목 |
| s3_file_url | `VARCHAR(500)` | NULLABLE | PDF 파일 S3 경로 |
| content | `TEXT` | NULLABLE | OCR/Parsing된 이력서 원문 |
| is_analyzed | `BOOLEAN` | DEFAULT FALSE | 분석 완료 여부 |
| created_at | `DATETIME` | DEFAULT NOW() | 생성 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |
### 4. job_posting (채용 공고)
| **컬럼명** | **타입** | **제약조건** | **설명** |
| -- | --- | --- | --- |
| id | `BIGINT` | PK, AUTO_INCREMENT | 공고 ID |
| user_id | `BIGINT` | FK (user.id) | 등록자 ID |
| company_name | `VARCHAR(100)` | DEFAULT 'Self-Input' | 기업명 |
| job_title | `VARCHAR(100)` | NOT NULL | 직무명 |
| job_description | `TEXT` | NULLABLE | 채용 공고 원문 (JD) |
| required_skills | `JSON` | NULLABLE | 역량 및 기술 태그 목록 |
| created_at | `DATETIME` | DEFAULT NOW() | 생성 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |
### 5. resume_analysis_report (이력서 분석 리포트)
| **컬럼명** | **타입** | **제약조건** | **설명** |
| -- | --- | --- | --- |
| id | `BIGINT` | PK, AUTO_INCREMENT | 리포트 ID |
| resume_id | `BIGINT` | FK (resume.id), CASCADE | 분석 대상 이력서 |
| job_posting_id | `BIGINT` | FK (job_posting.id) | 타겟 공고 |
| match_score | `INT` | NULLABLE | 직무 적합도 점수 (0~100) |
| keyword_analysis | `JSON` | NULLABLE | 키워드 분석 결과 |
| sentence_correction | `JSON` | NULLABLE | 문장 교정 데이터 |
| revised_full_content | `TEXT` | NULLABLE | AI 첨삭 완성본 (After) |
| status | `ENUM` | DEFAULT 'PENDING' | 진행 상태 |
| created_at | `DATETIME` | DEFAULT NOW() | 생성 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |
### 6. interview_session (면접 세션)
| **컬럼명** | **타입** | **제약조건** | **설명** |
| -- | --- | --- | --- |
| id | `BIGINT` | PK, AUTO_INCREMENT | 세션 ID |
| user_id | `BIGINT` | FK (user.id) | 사용자 ID |
| resume_id | `BIGINT` | FK (resume.id) | 기반 이력서 |
| job_posting_id | `BIGINT` | FK (job_posting.id) | 타겟 공고 |
| interview_mode | `ENUM` | DEFAULT 'GENERAL' | GENERAL, TAIL_BITING, PRESSURE |
| interview_type | `ENUM` | DEFAULT 'TEXT' | TEXT(채팅), VOICE(음성) |
| status | `ENUM` | DEFAULT 'IN_PROGRESS' | 진행 상태 |
| final_score | `INT` | DEFAULT 0 | 면접 종합 점수 |
| created_at | `DATETIME` | DEFAULT NOW() | 시작 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |
### 7. interview_record (면접 상세 기록)
| **컬럼명** | **타입** | **제약조건** | **설명** |
| -- | --- | --- | --- |
| id | `BIGINT` | PK, AUTO_INCREMENT | 레코드 ID |
| interview_session_id | `BIGINT` | FK, CASCADE | 세션 ID |
| question_text | `TEXT` | NOT NULL | AI 질문 내용 |
| question_intent | `TEXT` | NULLABLE | 질문 의도 및 팁(Hint) |
| answer_text | `TEXT` | NULLABLE | 사용자 답변 |
| follow_up_depth | `INT` | DEFAULT 0 | 꼬리물기 깊이 |
| audio_file_url | `VARCHAR(500)` | NULLABLE | 녹음 파일 경로 |
| wpm | `INT` | NULLABLE | 발화 속도 (Words Per Minute) |
| stt_accuracy | `FLOAT` | NULLABLE | 발음 정확도 |
| silence_count | `INT` | NULLABLE | 침묵 횟수 |
| emotion_analysis | `JSON` | NULLABLE | 목소리 감정 분석 결과 |
| feedback_text | `TEXT` | NULLABLE | AI 피드백 |
| evaluation_score | `FLOAT` | DEFAULT 0.0 | 답변 점수 |
| turn_sequence | `INT` | NOT NULL | 대화 순서 |
| created_at | `DATETIME` | DEFAULT NOW() | 생성 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |
### 8. question_archive (질문 보관함)
| **컬럼명** | **타입** | **제약조건** | **설명** |
| -- | --- | --- | --- |
| id | `BIGINT` | PK, AUTO_INCREMENT | 보관 ID |
| user_id | `BIGINT` | FK (user.id) | 사용자 ID |
| interview_record_id | `BIGINT` | FK (interview_record.id) | 원본 질문 ID |
| memo | `TEXT` | NULLABLE | 사용자 복습 메모 |
| created_at | `DATETIME` | DEFAULT NOW() | 스크랩 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |
### 9. notification (알림)
| **컬럼명** | **타입** | **제약조건** | **설명** |
| -- | --- | --- | --- |
| id | `BIGINT` | PK, AUTO_INCREMENT | 알림 ID |
| user_id | `BIGINT` | FK (user.id), CASCADE | 수신자 ID |
| message | `VARCHAR(255)` | NOT NULL | 알림 메시지 |
| type | `VARCHAR(50)` | NULLABLE | 알림 유형 |
| is_read | `BOOLEAN` | DEFAULT FALSE | 읽음 여부 |
| created_at | `DATETIME` | DEFAULT NOW() | 생성 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |
### 10. usage_log (크레딧 원장 & 활동 로그)
| **컬럼명** | **타입** | **제약조건** | **설명** |
| -- | --- | --- | --- |
| id | `BIGINT` | PK, AUTO_INCREMENT | 로그 ID |
| user_id | `BIGINT` | FK (user.id) | 사용자 ID |
| request_id | `VARCHAR(100)` | NOT NULL | API 요청 추적 ID (Trace ID) |
| service_type | `VARCHAR(50)` | NOT NULL | 서비스명 (MOCK_INTERVIEW 등) |
| description | `VARCHAR(255)` | NULLABLE | 내역 상세 |
| change_amount | `INT` | NOT NULL | 변동량 (-1, +10) |
| balance_after | `INT` | NOT NULL | 변동 후 잔액 스냅샷 |
| target_id | `BIGINT` | NULLABLE | 대상 데이터 ID |
| target_type | `VARCHAR(50)` | NULLABLE | 대상 데이터 타입 |
| created_at | `DATETIME` | DEFAULT NOW() | 발생 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |
### 11. admin_action_log (관리자 감사 로그)
| **컬럼명** | **타입** | **제약조건** | **설명** |
| -- | --- | --- | --- |
| id | `BIGINT` | PK, AUTO_INCREMENT | 로그 ID |
| admin_id | `BIGINT` | FK (user.id) | 관리자 ID |
| target_id | `BIGINT` | NULLABLE | 작업 대상 ID |
| target_type | `VARCHAR(50)` | NULLABLE | 작업 대상 타입 |
| action_type | `VARCHAR(50)` | NOT NULL | 작업 유형 (BAN, DELETE 등) |
| action_detail | `JSON` | NULLABLE | 작업 상세 (변경 내용 등) |
| ip_address | `VARCHAR(50)` | NULLABLE | 접속 IP |
| created_at | `DATETIME` | DEFAULT NOW() | 수행 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |