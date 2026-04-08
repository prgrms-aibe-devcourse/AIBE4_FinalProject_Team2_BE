# AIBE4_FinalProject_Team2_BE
2팀 임시 프로젝트 BE 

## 팀 소개


| 박형민 | 한경현 | 이윤우 | 최현준 | 김익현 |
|:------:|:------:|:------:|:------:|:------:|
| <img src="https://avatars.githubusercontent.com/gudals2040" width="100"/> | <img src="https://avatars.githubusercontent.com/khyun722" width="100"/> | <img src="https://avatars.githubusercontent.com/sableye9" width="100"/> | <img src="https://avatars.githubusercontent.com/c-wonjun" width="100"/> | <img src="https://avatars.githubusercontent.com/dlrgus041" width="100"/> |
| [@gudals2040](https://github.com/gudals2040) | [@khyun722](https://github.com/khyun722) | [@sableye9](https://github.com/sableye9) | [@c-wonjun](https://github.com/c-wonjun) | [@dlrgus041](https://github.com/dlrgus041) |

## 프로젝트 개요**Sync Talk**는 생성형 AI와 실시간 음성 스트리밍 기술을 활용한 맞춤형 모의 면접 서비스입니다. 
단순한 텍스트 기반의 질의응답을 넘어, 실제 면접과 유사한 양방향 대화 환경을 제공하여 구직자들의 실전 감각을 극대화하는 것을 목표로 합니다.

## 시스템 아키텍쳐
<img width="1024" alt="Image" src="https://github.com/user-attachments/assets/501c2afe-3621-4614-9b00-8c8058081b5d" />

## 기술 스택

| 구분 | 기술 |
| :--- | :--- |
| **Backend** | <img src="https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Spring%20Boot%203.5.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> <img src="https://img.shields.io/badge/PostgreSQL%20-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"> <img src="https://img.shields.io/badge/AWS%20SQS-FF9900?style=for-the-badge&logo=amazonsqs&logoColor=white"> |
| **외부 서비스** | <img src="https://img.shields.io/badge/Retell%20AI-5B21B6?style=for-the-badge"> <img src="https://img.shields.io/badge/Google%20Gemini-4285F4?style=for-the-badge&logo=googlegemini&logoColor=white"> <img src="https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"> <img src="https://img.shields.io/badge/OAuth2-3399FF?style=for-the-badge"> <img src="https://img.shields.io/badge/SSE-FF8C00?style=for-the-badge"> <img src="https://img.shields.io/badge/SMTP-667788?style=for-the-badge"> |
| **배포** | <img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"> <img src="https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"> <img src="https://img.shields.io/badge/AWS%20VPC-FF9900?style=for-the-badge"> <img src="https://img.shields.io/badge/Let's%20Encrypt-003A70?style=for-the-badge&logo=letsencrypt&logoColor=white"> |
| **모니터링** | <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white"> <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white"> <img src="https://img.shields.io/badge/Loki-F0A30A?style=for-the-badge"> <img src="https://img.shields.io/badge/Promtail-4D4D4D?style=for-the-badge"> |

## 주요 기능 (Key Features)

| 도메인 | 주요 기능 |
| :--- | :--- |
| **사용자 & 인증** | 소셜 로그인(OAuth2), JWT 기반 인증/인가, 마이페이지 및 면접 통계 조회 |
| **실시간 AI 면접** | 클라이언트-서버 간 실시간 양방향 음성 스트리밍 (Retell AI 연동) |
| **맞춤형 질문 (RAG)** | 사용자 이력서 및 직무 공고 벡터화(Gemini Embedding), 코사인 유사도 기반(pgvector) 맞춤형 면접 질문 생성 |
| **이벤트 기반 분석** | 메시지 큐(AWS SQS)를 활용한 비동기 면접 결과(STT, 분석, 평가) 처리 및 DB 저장 |
| **데이터 아카이빙** | 면접 종료 시 녹음된 음성 파일 및 결과 데이터 클라우드 스토리지(AWS S3) 보관 |
| **알림 및 제어** | Redis 기반 API 호출 제한(Rate Limiter), 면접 분석 완료 시 클라이언트 실시간 알림(SSE), SMTP 이메일 알림 |
| **모니터링** | Actuator, Prometheus, Grafana를 활용한 시스템 메트릭 시각화 및 실시간 로그 모니터링 |


## 실행 방법
```Bash
git clone https://github.com/prgrms-aibe-devcourse/aibe4_finalproject_team2_be.git
cd aibe4_finalproject_team2_be
```
2. 환경 변수 설정 (.env)
보안 관리를 위해 민감한 API 키와 DB 비밀번호는 Github에 올라가지 않습니다.
프로젝트 최상위 디렉토리(루트)에 .env 파일을 생성하고, 아래의 템플릿을 복사하여 본인의 키값으로 채워주세요. (해당 파일은 .gitignore에 등록되어 안전합니다.)
```
# ======================
# PostgreSQL (로컬 Docker 실행 시)
# ======================
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=aibe_team2
POSTGRES_USER=root
POSTGRES_PASSWORD=password

# ======================
# Redis
# ======================
REDIS_HOST=localhost
REDIS_PORT=6379

# ======================
# JWT / Security
# ======================
JWT_SECRET=your_jwt_secret_key_here_must_be_long_enough
JWT_ACCESS_TOKEN_VALIDITY=3600000
JWT_REFRESH_TOKEN_VALIDITY=604800000

# ======================
# SMTP (이메일 알림 용도)
# ======================
AUTH_ID=your_google_email@gmail.com
AUTH_PW=your_google_app_password

# ======================
# AWS (S3, SQS)
# ======================
# 로컬 테스트용 AWS 키
AWS_ACCESS_KEY_ID=your_aws_access_key_id
AWS_SECRET_ACCESS_KEY=your_aws_secret_access_key
AWS_REGION=ap-northeast-2

# ======================
# OAuth2 (소셜 로그인)
# ======================
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

# ======================
# AI & External API
# ======================
GEMINI_API_KEY=your_gemini_api_key
GEMINI_API_URL=[https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent](https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent)
RETELL_API_KEY=your_retell_api_key
RETELL_AGENT_ID=your_retell_agent_id
RETELL_WEBHOOK_KEY=your_retell_webhook_key
```
3. 인프라(DB, Cache) 실행
```Bash
docker-compose up -d
```
4. 애플리케이션 빌드 및 실행
Gradle Wrapper를 이용하여 프로젝트를 빌드하고 Spring Boot 서버를 실행합니다. IDE(IntelliJ 등)에서 실행할 경우, .env 파일을 인식할 수 있도록 EnvFile 플러그인을 사용하시거나 환경 변수를 직접 등록해 주세요.

```Bash
# Mac / Linux
./gradlew clean build -x test
./gradlew bootRun --args='--spring.profiles.active=dev'

# Windows (CMD / PowerShell)
gradlew.bat clean build -x test
gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

## 보안
- **OAuth2 & JWT:** 구글/카카오 소셜 로그인 및 JWT 기반 무상태(Stateless) 인증/인가
- **Redis 토큰 관리:** Refresh Token 저장 및 안전한 로그아웃(블랙리스트) 처리
- **Rate Limiting:** Redis를 활용한 API 트래픽 제어 및 외부 AI 서비스 과금 방어
- **웹 및 인프라 보안:** Spring Security 엔드포인트 보호, CORS/XSS 방지, 환경변수(`.env`) 분리 및 전 구간 HTTPS 적용

## API 문서
-   **Swagger UI**  
    [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Local DB

Host: localhost  
Port: 3307  
Database: aibe  
Username: aibe  
Password: aibe1234

## 라이선스
이 프로젝트는 **MIT 라이선스**를 따릅니다.

## 개발팀
-   Backend 개발: **LastDance Team**
-   프로젝트 기간: 2025년 2월 2일 ~ 2025년 3월 23일
