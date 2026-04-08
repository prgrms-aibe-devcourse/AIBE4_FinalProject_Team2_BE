# AIBE4_FinalProject_Team2_BE
2팀 임시 프로젝트 BE 

## 팀 소개


| 박형민 | 한경현 | 이윤우 | 최현준 | 김익현 |
|:------:|:------:|:------:|:------:|:------:|
| <img src="https://avatars.githubusercontent.com/gudals2040" width="100"/> | <img src="https://avatars.githubusercontent.com/khyun722" width="100"/> | <img src="https://avatars.githubusercontent.com/sableye9" width="100"/> | <img src="https://avatars.githubusercontent.com/c-wonjun" width="100"/> | <img src="https://avatars.githubusercontent.com/dlrgus041" width="100"/> |
| [@gudals2040](https://github.com/gudals2040) | [@khyun722](https://github.com/khyun722) | [@sableye9](https://github.com/sableye9) | [@c-wonjun](https://github.com/c-wonjun) | [@dlrgus041](https://github.com/dlrgus041) |

## 프로젝트 개요
**Sync Talk**는 채용 공고 맞춤형 자소서 분석부터 실전 AI,STT 모의 면접까지 한 번에 해결하는 취업 지원 플랫폼입니다.

## 시스템 아키텍쳐
<img width="1024" alt="Image" src="https://github.com/user-attachments/assets/501c2afe-3621-4614-9b00-8c8058081b5d" />

## 🛠 기술 스택 (Tech Stack)

### 💻 Backend
* **Language & Framework:** Java 17, Spring Boot 3.5.0
* **Security & Data:** Spring Security, Spring Data JPA
* **Database:** PostgreSQL (pgvector를 활용한 Hybrid RAG 벡터 검색 지원)
* **Cache & Queue:** Redis (API Rate Limiter 및 데이터 캐싱), AWS SQS (비동기 면접 분석을 위한 메시지 큐)

### 🔗 External Services
* **AI & Voice:** Google Gemini API (LLM 및 텍스트 임베딩), Retell AI (클라이언트 실시간 음성 스트리밍)
* **Storage & API:** AWS S3 (음성 파일 및 면접 기록 아카이빙), 청년정책 API
* **Auth & Comm:** OAuth2, SSE (클라이언트 실시간 알림), SMTP

### 🚀 Infrastructure & Deployment
* **AWS Cloud:** EC2 (API 및 Worker 서버 분리), RDS, ECR, Route 53, VPC 기반 네트워크 분리
* **Web Server & SSL:** Nginx (리버스 프록시), Let's Encrypt

### 📊 Observability & Monitoring
* **Metrics & Dashboards:** Prometheus, Grafana, Spring Boot Actuator
* **Log Management:** Loki, Promtail

* 
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
