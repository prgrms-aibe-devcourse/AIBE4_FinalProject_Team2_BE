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

## Local Dev (Docker Compose)

### 1. Create .env

#### macOS / Linux
```bash
cp .env.example .env
```

#### Windows (PowerShell)
```
Copy-Item .env.example .env
```

### Run
```bash
docker compose up -d
```
### Stop
```
docker compose down
```

### Local DB

Host: localhost  
Port: 3307  
Database: aibe  
Username: aibe  
Password: aibe1234
