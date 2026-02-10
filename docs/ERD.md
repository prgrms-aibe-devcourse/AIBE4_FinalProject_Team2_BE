| **컬럼명** | **타입** | **제약조건** | **설명** |
| --- | --- | --- | --- |
| **id** | `BIGINT` | **PK**, AUTO_INCREMENT | 사용자 고유 ID |
| email | `VARCHAR(255)` | **UNIQUE**, NOT NULL | 로그인 이메일 |
| password | `VARCHAR(255)` | NULLABLE | 비밀번호 (소셜 로그인 시 NULL) |
| nickname | `VARCHAR(50)` | NOT NULL | 닉네임 |
| role | `ENUM` | DEFAULT 'USER' | 권한 (USER, ADMIN) |
| subscription_plan | `ENUM` | DEFAULT 'FREE' | 구독 등급 (FREE, PRO, ENTERPRISE) |
| **remaining_credits** | `INT` | DEFAULT 0 | **잔여 크레딧 (지갑 잔액)** |
| desired_job_role | `VARCHAR(100)` | NULLABLE | 희망 직무 (프로필 설정) |
| preferred_location | `VARCHAR(100)` | NULLABLE | 선호 근무 지역 |
| provider | `ENUM` | DEFAULT 'LOCAL' | 가입 경로 (LOCAL, GOOGLE, KAKAO) |
| profile_image_url | `VARCHAR(500)` | NULLABLE | 프로필 이미지 URL |
| created_at | `DATETIME` | DEFAULT NOW() | 가입 일시 |
| updated_at | `DATETIME` | ON UPDATE NOW() | 수정 일시 |
| deleted_at | `DATETIME` | NULLABLE | 탈퇴 일시 (Soft Delete) |
