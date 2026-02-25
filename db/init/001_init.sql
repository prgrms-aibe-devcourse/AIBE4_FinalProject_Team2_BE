-- db/init/001_init.sql
-- 목적: Docker MySQL 최초 기동 시 스키마를 확정해서 생성

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) member
CREATE TABLE member (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        email VARCHAR(255) NOT NULL UNIQUE,
                        password VARCHAR(255),
                        nickname VARCHAR(50) NOT NULL,
                        role ENUM('MEMBER', 'ADMIN') DEFAULT 'MEMBER',
                        desired_job VARCHAR(100),
                        preferred_location VARCHAR(100),
                        subscription_plan ENUM('FREE', 'PRO', 'ENTERPRISE') DEFAULT 'FREE',
                        credit_balance INT DEFAULT 0,
                        auth_provider ENUM('LOCAL', 'GITHUB', 'GOOGLE', 'KAKAO') DEFAULT 'LOCAL',
                        profile_image_url VARCHAR(500),
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        deleted_at DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) social_auth
CREATE TABLE social_auth (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             member_id BIGINT NOT NULL,
                             provider_member_id VARCHAR(255) NOT NULL,
                             provider_type VARCHAR(50),
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             deleted_at DATETIME DEFAULT NULL,
                             CONSTRAINT fk_social_member
                                 FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
                             UNIQUE KEY unique_provider_member (provider_type, provider_member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) resume
CREATE TABLE resume (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        member_id BIGINT NOT NULL,
                        title VARCHAR(255) NOT NULL,
                        s3_file_url VARCHAR(500),
                        content TEXT,
                        is_analyzed BOOLEAN DEFAULT FALSE,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_resume_member
                            FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) job_posting
CREATE TABLE job_posting (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             member_id BIGINT NOT NULL,
                             company_name VARCHAR(100) NOT NULL DEFAULT 'Self-Input',
                             job_title VARCHAR(100) NOT NULL,
                             job_description TEXT,
                             posting_url TEXT,
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             CONSTRAINT fk_job_member
                                 FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4-1) job_skill
CREATE TABLE job_skill (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           job_posting_id BIGINT NOT NULL,
                           skill_name VARCHAR(50) NOT NULL,
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_skill_job
                               FOREIGN KEY (job_posting_id) REFERENCES job_posting(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_skill_name ON job_skill(skill_name);

-- 5) analysis_report
CREATE TABLE analysis_report (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 resume_id BIGINT NOT NULL,
                                 job_posting_id BIGINT NOT NULL,
                                 match_score INT,
                                 keyword_analysis JSON,
                                 sentence_correction JSON,
                                 generated_subtitle JSON,
                                 revised_full_content TEXT,
                                 status ENUM('PENDING', 'PROCESSING', 'DELAYED', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_report_resume
                                     FOREIGN KEY (resume_id) REFERENCES resume(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_report_job
                                     FOREIGN KEY (job_posting_id) REFERENCES job_posting(id),
                                 UNIQUE KEY unique_analysis (resume_id, job_posting_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) interview_session
CREATE TABLE interview_session (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   member_id BIGINT NOT NULL,
                                   resume_id BIGINT NOT NULL,
                                   job_posting_id BIGINT NOT NULL,
                                   interview_mode ENUM('GENERAL', 'TAIL_BITING', 'PRESSURE') NOT NULL DEFAULT 'GENERAL',
                                   interview_type ENUM('TEXT', 'VOICE') NOT NULL DEFAULT 'TEXT',
                                   status ENUM('CREATED', 'IN_PROGRESS', 'DONE', 'ABORTED') DEFAULT 'IN_PROGRESS',
                                   final_score INT DEFAULT 0,
                                   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   CONSTRAINT fk_session_member
                                       FOREIGN KEY (member_id) REFERENCES member(id),
                                   CONSTRAINT fk_session_resume
                                       FOREIGN KEY (resume_id) REFERENCES resume(id),
                                   CONSTRAINT fk_session_job
                                       FOREIGN KEY (job_posting_id) REFERENCES job_posting(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) interview_record
CREATE TABLE interview_record (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  interview_session_id BIGINT NOT NULL,
                                  question_text TEXT NOT NULL,
                                  question_intent TEXT,
                                  answer_text TEXT,
                                  follow_up_depth INT DEFAULT 0,
                                  s3_file_url VARCHAR(500),
                                  wpm INT,
                                  stt_accuracy FLOAT,
                                  silence_count INT,
                                  emotion_analysis JSON,
                                  feedback_text TEXT,
                                  evaluation_score FLOAT DEFAULT 0.0,
                                  response_time_ms INT,
                                  turn_sequence INT NOT NULL,
                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_record_session
                                      FOREIGN KEY (interview_session_id) REFERENCES interview_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8) question_scrap
CREATE TABLE question_scrap (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                member_id BIGINT NOT NULL,
                                interview_record_id BIGINT NOT NULL,
                                memo TEXT,
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                CONSTRAINT fk_scrap_member
                                    FOREIGN KEY (member_id) REFERENCES member(id),
                                CONSTRAINT fk_scrap_record
                                    FOREIGN KEY (interview_record_id) REFERENCES interview_record(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9) notification
CREATE TABLE notification (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              member_id BIGINT NOT NULL,
                              message VARCHAR(255) NOT NULL,
                              notification_type VARCHAR(50),
                              is_read BOOLEAN DEFAULT FALSE,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              CONSTRAINT fk_noti_member
                                  FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10) usage_log
CREATE TABLE usage_log (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           member_id BIGINT NOT NULL,
                           request_trace_id VARCHAR(100) NOT NULL,
                           service_type VARCHAR(50) NOT NULL,
                           token_usage INT DEFAULT 0,
                           amount INT NOT NULL,
                           balance_after INT NOT NULL,
                           target_type VARCHAR(50),
                           target_id BIGINT,
                           description VARCHAR(255),
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           CONSTRAINT fk_usage_member
                               FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_usage_member ON usage_log(member_id);
CREATE INDEX idx_usage_created ON usage_log(created_at);
CREATE INDEX idx_usage_service ON usage_log(service_type);

-- 11) audit_log
CREATE TABLE audit_log (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           admin_id BIGINT NOT NULL,
                           target_id BIGINT,
                           target_type VARCHAR(50),
                           action_type VARCHAR(50) NOT NULL,
                           action_detail JSON,
                           ip_address VARCHAR(50),
                           member_agent VARCHAR(255),
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           CONSTRAINT fk_audit_admin
                               FOREIGN KEY (admin_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12) attachment
CREATE TABLE attachment (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            owner_member_id BIGINT NOT NULL,
                            s3_key VARCHAR(500) NOT NULL,
                            file_type ENUM('RESUME_ORIGINAL', 'RESUME_REVISED', 'INTERVIEW_AUDIO') NOT NULL,
                            target_type VARCHAR(50) NOT NULL,
                            target_id BIGINT NOT NULL,
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            CONSTRAINT fk_attachment_owner
                                FOREIGN KEY (owner_member_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_attachment_owner ON attachment(owner_member_id);
CREATE INDEX idx_attachment_target ON attachment(target_type, target_id);

SET FOREIGN_KEY_CHECKS = 1;