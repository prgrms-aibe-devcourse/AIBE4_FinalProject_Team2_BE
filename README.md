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

## 🛠 기술 스택

| 구분 | 기술 |
| :--- | :--- |
| **Backend** | <img src="https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Spring%20Boot%203.5.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> <img src="https://img.shields.io/badge/PostgreSQL%20(pgvector)-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"> <img src="https://img.shields.io/badge/AWS%20SQS-FF9900?style=for-the-badge&logo=amazonsqs&logoColor=white"> |
| **외부 서비스** | <img src="https://img.shields.io/badge/Retell%20AI-5B21B6?style=for-the-badge"> <img src="https://img.shields.io/badge/Google%20Gemini-4285F4?style=for-the-badge&logo=googlegemini&logoColor=white"> <img src="https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"> <img src="https://img.shields.io/badge/OAuth2-3399FF?style=for-the-badge"> <img src="https://img.shields.io/badge/SSE-FF8C00?style=for-the-badge"> <img src="https://img.shields.io/badge/SMTP-667788?style=for-the-badge"> |
| **배포** | <img src="https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"> <img src="https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"> <img src="https://img.shields.io/badge/AWS%20VPC-FF9900?style=for-the-badge"> <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"> <img src="https://img.shields.io/badge/Let's%20Encrypt-003A70?style=for-the-badge&logo=letsencrypt&logoColor=white"> |
| **모니터링** | <img src="https://img.shields.io/badge/Spring%20Boot%20Actuator-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white"> <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white"> <img src="https://img.shields.io/badge/Loki-F0A30A?style=for-the-badge"> <img src="https://img.shields.io/badge/Promtail-4D4D4D?style=for-the-badge"> |

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
