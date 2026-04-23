-- db/init/001_init.sql
-- 목적: Docker PostgreSQL 최초 기동 시 스키마를 확정해서 생성

-- pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- 1) member
CREATE TABLE member (
                        id BIGSERIAL PRIMARY KEY,
                        email VARCHAR(255) NOT NULL UNIQUE,
                        password_hash VARCHAR(255),
                        nickname VARCHAR(50) NOT NULL,
                        role VARCHAR(20) NOT NULL DEFAULT 'MEMBER'
                            CHECK (role IN ('MEMBER', 'ADMIN')),
                        desired_job VARCHAR(100),
                        preferred_location VARCHAR(100),
                        subscription_plan VARCHAR(20) NOT NULL DEFAULT 'FREE'
                            CHECK (subscription_plan IN ('FREE', 'PRO', 'ENTERPRISE')),
                        credit_balance INT DEFAULT 0,
                        auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL'
                            CHECK (auth_provider IN ('LOCAL', 'GOOGLE', 'KAKAO')),
                        profile_image_url VARCHAR(500),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        last_login_at TIMESTAMP DEFAULT NULL,
                        deleted_at TIMESTAMP DEFAULT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE', 'DORMANCY', 'DELETED'))
);

-- 2) social_auth
CREATE TABLE social_auth (
                             id BIGSERIAL PRIMARY KEY,
                             member_id BIGINT NOT NULL,
                             provider_member_id VARCHAR(255) NOT NULL,
                             provider_type VARCHAR(50),
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             deleted_at TIMESTAMP DEFAULT NULL,
                             CONSTRAINT fk_social_member
                                 FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
                             CONSTRAINT unique_provider_member UNIQUE (provider_type, provider_member_id)
);

-- 3) resume
CREATE TABLE resume (
                        id BIGSERIAL PRIMARY KEY,
                        member_id BIGINT NOT NULL,
                        title VARCHAR(255) NOT NULL,
                        s3_file_url VARCHAR(500),
                        content TEXT,
                        embedding VECTOR(768),
                        is_analyzed BOOLEAN DEFAULT FALSE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_resume_member
                            FOREIGN KEY (member_id) REFERENCES member(id)
);

-- 4) job_posting
CREATE TABLE job_posting (
                             id BIGSERIAL PRIMARY KEY,
                             member_id BIGINT NOT NULL,
                             company_name VARCHAR(100) NOT NULL DEFAULT 'Self-Input',
                             job_title VARCHAR(100) NOT NULL,
                             posting_url TEXT,
                             job_description TEXT,
                             embedding VECTOR(768),
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_job_member
                                 FOREIGN KEY (member_id) REFERENCES member(id)
);

-- 4-1) job_skill
CREATE TABLE job_skill (
                           id BIGSERIAL PRIMARY KEY,
                           job_posting_id BIGINT NOT NULL,
                           skill_name VARCHAR(50) NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_skill_job
                               FOREIGN KEY (job_posting_id) REFERENCES job_posting(id) ON DELETE CASCADE
);

CREATE INDEX idx_skill_name ON job_skill(skill_name);

-- 5) analysis_report
CREATE TABLE analysis_report (
                                 id BIGSERIAL PRIMARY KEY,
                                 resume_id BIGINT NOT NULL,
                                 job_posting_id BIGINT,
                                 analysis_type VARCHAR(20) NOT NULL
                                     CHECK (analysis_type IN ('NORMAL', 'FIT_MATCH')),
                                 status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                     CHECK (status IN ('PENDING', 'PROCESSING', 'DELAYED', 'COMPLETED', 'FAILED', 'CANCELLED')),
                                 overall_feedback TEXT,
                                 sentence_corrections JSONB,
                                 paragraph_summaries JSONB,
                                 revised_full_content TEXT,
                                 job_description TEXT,
                                 job_description_text TEXT,
                                 match_score INT,
                                 matching_feedback TEXT,
                                 keyword_analysis JSONB,
                                 expected_questions JSONB,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_report_resume
                                     FOREIGN KEY (resume_id) REFERENCES resume(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_report_job
                                     FOREIGN KEY (job_posting_id) REFERENCES job_posting(id),
                                 CONSTRAINT unique_analysis UNIQUE (resume_id, job_posting_id)
);

-- 6) interview_session
CREATE TABLE interview_session (
                                   id BIGSERIAL PRIMARY KEY,
                                   member_id BIGINT NOT NULL,

                                   resume_id BIGINT,
                                   job_posting_id BIGINT,

                                   job_description TEXT,

                                   interview_mode VARCHAR(20) NOT NULL DEFAULT 'NORMAL'
                                       CHECK (interview_mode IN ('NORMAL', 'FOLLOW_UP', 'STRESS')),
                                   interview_type VARCHAR(20) NOT NULL DEFAULT 'TEXT'
                                       CHECK (interview_type IN ('TEXT', 'VOICE')),

                                   job_role VARCHAR(50),
                                   experience VARCHAR(50),

                                   ai_provider VARCHAR(50),
                                   model_variant VARCHAR(100),

                                   status VARCHAR(20) NOT NULL DEFAULT 'CREATED'
                                       CHECK (status IN ('CREATED', 'IN_PROGRESS', 'DONE', 'ABORTED')),

                                   final_score INT DEFAULT 0,

                                   overall_feedback TEXT,
                                   job_relevance_score INT DEFAULT 0,
                                   attitude_confidence_score INT DEFAULT 0,
                                   logical_structure_score INT DEFAULT 0,
                                   clarity_score INT DEFAULT 0,
                                   persuasiveness_score INT DEFAULT 0,
                                   consistency_score INT DEFAULT 0,

                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_session_member
                                       FOREIGN KEY (member_id) REFERENCES member(id),
                                   CONSTRAINT fk_session_resume
                                       FOREIGN KEY (resume_id) REFERENCES resume(id),
                                   CONSTRAINT fk_session_job
                                       FOREIGN KEY (job_posting_id) REFERENCES job_posting(id)
);

-- 7) interview_record
CREATE TABLE interview_record (
                                  id BIGSERIAL PRIMARY KEY,
                                  interview_session_id BIGINT NOT NULL,

                                  question_text TEXT NOT NULL,
                                  question_intent TEXT,
                                  answer_text TEXT,
                                  follow_up_depth INT DEFAULT 0,

                                  s3_file_url VARCHAR(500),
                                  wpm INT,
                                  stt_accuracy REAL,
                                  silence_count INT,
                                  emotion_analysis TEXT,

                                  evaluation_reason TEXT,
                                  feedback_text TEXT,
                                  recommended_guides TEXT,

                                  evaluation_score REAL DEFAULT 0.0,
                                  response_time_ms INT,
                                  turn_sequence INT NOT NULL,

                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_record_session
                                      FOREIGN KEY (interview_session_id) REFERENCES interview_session(id) ON DELETE CASCADE
);

-- 7-1) interview_result_statistics
CREATE TABLE interview_result_statistics (
                                             id BIGSERIAL PRIMARY KEY,
                                             interview_session_id BIGINT NOT NULL UNIQUE,

                                             avg_clarity DOUBLE PRECISION,
                                             avg_persuasiveness DOUBLE PRECISION,
                                             avg_consistency DOUBLE PRECISION,
                                             job_relevance_score DOUBLE PRECISION,
                                             logical_structure_score DOUBLE PRECISION,
                                             attitude_confidence_score DOUBLE PRECISION,

                                             overall_feedback TEXT,
                                             speech_habits TEXT,

                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                             CONSTRAINT fk_interview_result_statistics_session
                                                 FOREIGN KEY (interview_session_id)
                                                     REFERENCES interview_session(id)
                                                     ON DELETE CASCADE
);

CREATE INDEX idx_interview_result_statistics_created_at
    ON interview_result_statistics(created_at);

-- 8) question_scrap
CREATE TABLE question_scrap (
                                id BIGSERIAL PRIMARY KEY,
                                member_id BIGINT NOT NULL,
                                interview_record_id BIGINT NOT NULL,
                                memo TEXT,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_scrap_member
                                    FOREIGN KEY (member_id) REFERENCES member(id),
                                CONSTRAINT fk_scrap_record
                                    FOREIGN KEY (interview_record_id) REFERENCES interview_record(id)
);

-- 9) notification
CREATE TABLE notification (
                              id BIGSERIAL PRIMARY KEY,
                              member_id BIGINT NOT NULL,
                              message VARCHAR(255) NOT NULL,
                              notification_type VARCHAR(50),
                              is_read BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_noti_member
                                  FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);

-- 10) usage_log
CREATE TABLE usage_log (
                           id BIGSERIAL PRIMARY KEY,
                           member_id BIGINT NOT NULL,
                           request_trace_id VARCHAR(100) NOT NULL,
                           service_type VARCHAR(50) NOT NULL,
                           token_usage INT DEFAULT 0,
                           amount INT NOT NULL,
                           balance_after INT NOT NULL,
                           target_type VARCHAR(50),
                           target_id BIGINT,
                           description VARCHAR(255),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_usage_member
                               FOREIGN KEY (member_id) REFERENCES member(id)
);

CREATE INDEX idx_usage_member ON usage_log(member_id);
CREATE INDEX idx_usage_created ON usage_log(created_at);
CREATE INDEX idx_usage_service ON usage_log(service_type);

-- 11) audit_log
CREATE TABLE audit_log (
                           id BIGSERIAL PRIMARY KEY,
                           admin_id BIGINT NOT NULL,
                           target_id BIGINT,
                           target_type VARCHAR(50),
                           action_type VARCHAR(50) NOT NULL,
                           action_detail JSONB,
                           ip_address VARCHAR(50),
                           member_agent VARCHAR(255),
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_audit_admin
                               FOREIGN KEY (admin_id) REFERENCES member(id)
);

-- 12) attachment
CREATE TABLE attachment (
                            id BIGSERIAL PRIMARY KEY,
                            owner_member_id BIGINT NOT NULL,
                            s3_key VARCHAR(500) NOT NULL,
                            file_type VARCHAR(30) NOT NULL
                                CHECK (file_type IN ('RESUME_ORIGINAL', 'RESUME_REVISED', 'INTERVIEW_AUDIO')),
                            target_type VARCHAR(50) NOT NULL,
                            target_id BIGINT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_attachment_owner
                                FOREIGN KEY (owner_member_id) REFERENCES member(id)
);

CREATE INDEX idx_attachment_owner ON attachment(owner_member_id);
CREATE INDEX idx_attachment_target ON attachment(target_type, target_id);

-- 13) queue_job_metric
CREATE TABLE queue_job_metric (
                                  id BIGSERIAL PRIMARY KEY,
                                  job_type VARCHAR(50) NOT NULL,
                                  status VARCHAR(30) NOT NULL,
                                  target_type VARCHAR(50) NOT NULL,
                                  target_id BIGINT NOT NULL,
                                  message_id VARCHAR(100),
                                  retry_count INT NOT NULL DEFAULT 0,
                                  error_message VARCHAR(1000),
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_queue_job_target ON queue_job_metric(target_type, target_id);
CREATE INDEX idx_queue_job_status ON queue_job_metric(status);
CREATE INDEX idx_queue_job_created_at ON queue_job_metric(created_at);

-- 14) operation_metric_daily
CREATE TABLE operation_metric_daily (
                                        id BIGSERIAL PRIMARY KEY,
                                        metric_date DATE NOT NULL,
                                        service_type VARCHAR(50) NOT NULL,
                                        total_log_count BIGINT NOT NULL DEFAULT 0,
                                        total_token_usage BIGINT NOT NULL DEFAULT 0,
                                        queue_enqueued_count BIGINT NOT NULL DEFAULT 0,
                                        queue_success_count BIGINT NOT NULL DEFAULT 0,
                                        queue_failed_count BIGINT NOT NULL DEFAULT 0,
                                        error_count BIGINT NOT NULL DEFAULT 0,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        CONSTRAINT uk_operation_metric_daily UNIQUE (metric_date, service_type)
);

-- 15) operation_metric_hourly
CREATE TABLE operation_metric_hourly (
                                         id BIGSERIAL PRIMARY KEY,
                                         metric_date DATE NOT NULL,
                                         metric_hour INT NOT NULL,
                                         service_type VARCHAR(50) NOT NULL,
                                         total_log_count BIGINT NOT NULL DEFAULT 0,
                                         total_token_usage BIGINT NOT NULL DEFAULT 0,
                                         queue_enqueued_count BIGINT NOT NULL DEFAULT 0,
                                         queue_success_count BIGINT NOT NULL DEFAULT 0,
                                         queue_failed_count BIGINT NOT NULL DEFAULT 0,
                                         error_count BIGINT NOT NULL DEFAULT 0,
                                         failure_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         CONSTRAINT uk_operation_metric_hourly UNIQUE (metric_date, metric_hour, service_type)
);

CREATE INDEX idx_operation_metric_hourly_date_hour
    ON operation_metric_hourly(metric_date, metric_hour);

CREATE INDEX idx_operation_metric_hourly_date_service
    ON operation_metric_hourly(metric_date, service_type);

-- 16) error_issue
CREATE TABLE error_issue (
                             id BIGSERIAL PRIMARY KEY,
                             fingerprint VARCHAR(255) NOT NULL UNIQUE,
                             title VARCHAR(255) NOT NULL,
                             error_code VARCHAR(100),
                             severity VARCHAR(30) NOT NULL,
                             status VARCHAR(30) NOT NULL,
                             error_domain VARCHAR(50) NOT NULL,
                             occurrence_count BIGINT NOT NULL DEFAULT 0,
                             first_occurred_at TIMESTAMP NOT NULL,
                             last_occurred_at TIMESTAMP NOT NULL,
                             last_error_log_id BIGINT,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT chk_error_issue_severity
                                 CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
                             CONSTRAINT chk_error_issue_status
                                 CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'IGNORED')),
                             CONSTRAINT chk_error_issue_domain
                                 CHECK (error_domain IN (
                                                         'GLOBAL',
                                                         'AUTH',
                                                         'RESUME',
                                                         'INTERVIEW',
                                                         'JOB_POSTING',
                                                         'FILE',
                                                         'STATISTICS',
                                                         'ADMIN',
                                                         'QUEUE',
                                                         'EXTERNAL_API',
                                                         'SYSTEM'
                                     ))
);

CREATE UNIQUE INDEX idx_error_issue_fingerprint
    ON error_issue(fingerprint);

CREATE INDEX idx_error_issue_status
    ON error_issue(status);

CREATE INDEX idx_error_issue_severity
    ON error_issue(severity);

CREATE INDEX idx_error_issue_last_occurred_at
    ON error_issue(last_occurred_at);


-- 17) error_log
CREATE TABLE error_log (
                           id BIGSERIAL PRIMARY KEY,
                           issue_id BIGINT,
                           member_id BIGINT,
                           error_code VARCHAR(100),
                           exception_type VARCHAR(255) NOT NULL,
                           message VARCHAR(1000) NOT NULL,
                           normalized_message VARCHAR(1000),
                           fingerprint VARCHAR(255) NOT NULL,
                           severity VARCHAR(30) NOT NULL,
                           error_domain VARCHAR(50) NOT NULL,
                           request_trace_id VARCHAR(100),
                           target_type VARCHAR(100),
                           target_id BIGINT,
                           stack_trace TEXT,
                           occurred_at TIMESTAMP NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_error_log_issue
                               FOREIGN KEY (issue_id) REFERENCES error_issue(id),
                           CONSTRAINT fk_error_log_member
                               FOREIGN KEY (member_id) REFERENCES member(id),
                           CONSTRAINT chk_error_log_severity
                               CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
                           CONSTRAINT chk_error_log_domain
                               CHECK (error_domain IN (
                                                       'GLOBAL',
                                                       'AUTH',
                                                       'RESUME',
                                                       'INTERVIEW',
                                                       'JOB_POSTING',
                                                       'FILE',
                                                       'STATISTICS',
                                                       'ADMIN',
                                                       'QUEUE',
                                                       'EXTERNAL_API',
                                                       'SYSTEM'
                                   ))
);

CREATE INDEX idx_error_log_issue
    ON error_log(issue_id);

CREATE INDEX idx_error_log_fingerprint
    ON error_log(fingerprint);

CREATE INDEX idx_error_log_occurred_at
    ON error_log(occurred_at);

CREATE INDEX idx_error_log_error_code
    ON error_log(error_code);