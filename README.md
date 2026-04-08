# AIBE4_FinalProject_Team2_BE
2팀 임시 프로젝트 BE 

## 팀 소개


| 박형민 | 한경현 | 이윤우 | 최현준 | 김익현 |
|:------:|:------:|:------:|:------:|:------:|
| <img src="https://avatars.githubusercontent.com/gudals2040" width="100"/> | <img src="https://avatars.githubusercontent.com/khyun722" width="100"/> | <img src="https://avatars.githubusercontent.com/sableye9" width="100"/> | <img src="https://avatars.githubusercontent.com/c-wonjun" width="100"/> | <img src="https://avatars.githubusercontent.com/dlrgus041" width="100"/> |
| [@gudals2040](https://github.com/gudals2040) | [@khyun722](https://github.com/khyun722) | [@sableye9](https://github.com/sableye9) | [@c-wonjun](https://github.com/c-wonjun) | [@dlrgus041](https://github.com/dlrgus041) |


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
