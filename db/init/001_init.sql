-- db/init/001_init.sql (recommended)
-- 목적: Docker MySQL 최초 기동 시 스키마를 “확정”해서 생성

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) user
CREATE TABLE user (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      email VARCHAR(255) NOT NULL UNIQUE,
                      password VARCHAR(255),
                      nickname VARCHAR(50) NOT NULL,
                      role ENUM('USER', 'ADMIN') DEFAULT 'USER',
                      desired_job_role VARCHAR(100),
                      preferred_location VARCHAR(100),
                      subscription_plan ENUM('FREE', 'PRO', 'ENTERPRISE') DEFAULT 'FREE',
                      remaining_credits INT DEFAULT 0,
                      provider ENUM('LOCAL', 'GOOGLE', 'KAKAO') DEFAULT 'LOCAL',
                      profile_image_url VARCHAR(500),
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      deleted_at DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) social_auth
CREATE TABLE social_auth (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             provider_id VARCHAR(255) NOT NULL,
                             provider_type VARCHAR(50),
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             CONSTRAINT fk_social_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
                             UNIQUE KEY unique_provider_user (provider_type, provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) resume
CREATE TABLE resume (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        title VARCHAR(255) NOT NULL,
                        s3_file_url VARCHAR(500),
                        content TEXT,
                        is_analyzed BOOLEAN DEFAULT FALSE,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT fk_resume_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4) job_posting
CREATE TABLE job_posting (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             company_name VARCHAR(100) NOT NULL DEFAULT 'Self-Input',
                             job_title VARCHAR(100) NOT NULL,
                             job_description TEXT,
                             required_skills JSON,
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             CONSTRAINT fk_job_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) resume_analysis_report
CREATE TABLE resume_analysis_report (
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
                                        CONSTRAINT fk_report_resume FOREIGN KEY (resume_id) REFERENCES resume(id) ON DELETE CASCADE,
                                        CONSTRAINT fk_report_job FOREIGN KEY (job_posting_id) REFERENCES job_posting(id),
                                        UNIQUE KEY unique_analysis (resume_id, job_posting_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) interview_session
CREATE TABLE interview_session (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   user_id BIGINT NOT NULL,
                                   resume_id BIGINT NOT NULL,
                                   job_posting_id BIGINT NOT NULL,
                                   interview_mode ENUM('GENERAL', 'TAIL_BITING', 'PRESSURE') NOT NULL DEFAULT 'GENERAL',
                                   interview_type ENUM('TEXT', 'VOICE') NOT NULL DEFAULT 'TEXT',
                                   status ENUM('IN_PROGRESS', 'COMPLETED') DEFAULT 'IN_PROGRESS',
                                   final_score INT DEFAULT 0,
                                   created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES user(id),
                                   CONSTRAINT fk_session_resume FOREIGN KEY (resume_id) REFERENCES resume(id),
                                   CONSTRAINT fk_session_job FOREIGN KEY (job_posting_id) REFERENCES job_posting(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) interview_record
CREATE TABLE interview_record (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  interview_session_id BIGINT NOT NULL,
                                  question_text TEXT NOT NULL,
                                  question_intent TEXT,
                                  answer_text TEXT,
                                  follow_up_depth INT DEFAULT 0,
                                  audio_file_url VARCHAR(500),
                                  wpm INT,
                                  stt_accuracy FLOAT,
                                  silence_count INT,
                                  emotion_analysis JSON,
                                  feedback_text TEXT,
                                  evaluation_score FLOAT DEFAULT 0.0,
                                  latency_ms INT,
                                  turn_sequence INT NOT NULL,
                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_record_session FOREIGN KEY (interview_session_id) REFERENCES interview_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8) question_archive
CREATE TABLE question_archive (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  user_id BIGINT NOT NULL,
                                  interview_record_id BIGINT NOT NULL,
                                  memo TEXT,
                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_archive_user FOREIGN KEY (user_id) REFERENCES user(id),
                                  CONSTRAINT fk_archive_record FOREIGN KEY (interview_record_id) REFERENCES interview_record(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9) notification
CREATE TABLE notification (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              message VARCHAR(255) NOT NULL,
                              type VARCHAR(50),
                              is_read BOOLEAN DEFAULT FALSE,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              CONSTRAINT fk_noti_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10) usage_log
CREATE TABLE usage_log (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           request_id VARCHAR(100) NOT NULL,
                           service_type VARCHAR(50) NOT NULL,
                           description VARCHAR(255),
                           change_amount INT NOT NULL,
                           balance_after INT NOT NULL,
                           target_id BIGINT,
                           target_type VARCHAR(50),
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           CONSTRAINT fk_usage_user FOREIGN KEY (user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11) admin_action_log
CREATE TABLE admin_action_log (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  admin_id BIGINT NOT NULL,
                                  target_id BIGINT,
                                  target_type VARCHAR(50),
                                  action_type VARCHAR(50) NOT NULL,
                                  action_detail JSON,
                                  ip_address VARCHAR(50),
                                  user_agent VARCHAR(255),
                                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_admin_log FOREIGN KEY (admin_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12) attachment (기존 files -> attachment로 변경)
CREATE TABLE attachment (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            owner_user_id BIGINT NOT NULL,
                            s3_key VARCHAR(500) NOT NULL,
                            file_type ENUM('RESUME_ORIGINAL', 'RESUME_REVISED', 'INTERVIEW_AUDIO') NOT NULL,
                            target_type VARCHAR(50) NOT NULL,
                            target_id BIGINT NOT NULL,
                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            CONSTRAINT fk_attachment_owner FOREIGN KEY (owner_user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_attachment_owner ON attachment(owner_user_id);
CREATE INDEX idx_attachment_target ON attachment(target_type, target_id);

SET FOREIGN_KEY_CHECKS = 1;