---

본 문서는 팀 프로젝트의 코드 품질 유지와 원활한 협업을 위해

Git 사용 규칙을 정의한다.

모든 작업은 다음 흐름을 따른다.

> **Issue → Branch → Commit → Pull Request → Merge**
> 

---

## 1. Issue 규칙

모든 작업은 Issue 생성으로 시작한다.

### Issue 작성 원칙

- 작업 단위별로 1 Issue 생성
- 템플릿 준수
- 담당자(Assignee) 지정

### 제목 규칙

```
[TYPE] 작업 내용
```

| Type | 설명 |
| --- | --- |
| FEAT | 기능 개발 |
| FIX | 버그 수정 |
| DOCS | 문서 작업 |
| REFACTOR | 리팩토링 |
| CHORE | 설정/의존성 |

예시:

```
[FEAT] 로그인 API 구현
[FIX] 토큰 만료 오류 수정
```

# 2. Branch 규칙

---

Issue 생성 후, 해당 작업 전용 Branch를 생성한다.

모든 작업은 Issue 기반으로 브랜치를 생성하며,

이슈 하나당 하나의 브랜치를 원칙으로 한다.

---

### 기본 브랜치

| 브랜치 | 설명 |
| --- | --- |
| main | 배포/안정 버전 |
| develop | 개발 통합 브랜치 |

---

### 작업 브랜치

모든 기능/수정 작업은 `develop` 브랜치에서 분기한다.

---

### Branch Naming Rule

브랜치 이름은 Issue 제목과 연관되도록 작성한다.

```
type/issue-number-summary
```

- type: 작업 유형
- issue-number: GitHub Issue 번호
- summary: Issue 제목 요약
- 소문자 사용
- 공백 →  - 사용
- 한글 사용 금지

**예시:**

```
feat/12-login-api
fix/24-jwt-expired
docs/30-update-readme
```

### Branch Type

| Type | 설명 |
| --- | --- |
| feat | 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 수정 |
| refactor | 리팩토링 |
| chore | 설정/빌드 |

### release / hotfix 브랜치 (필요 시)

배포 또는 긴급 수정이 필요한 경우에만 사용한다.

| 타입 | 기준 |
| --- | --- |
| release | develop → 배포 준비 |
| hotfix | main → 긴급 수정 |

# 3. Commit 규칙

작업의 최소 단위로 커밋한다.

---

**Commit Format**

```
type:summary

body (optional)
```

**Type**

| Type | 설명 |
| --- | --- |
| feat | 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 수정 |
| refactor | 리팩토링 |
| chore | 설정/빌드 |

# 4. Pull Request 규칙

모든 작업은 PR을 통해 병합한다.

직접 merge 하지 않는다.

### PR 제목

Issue 제목과 동일하게 작성

```
[FEAT] 로그인 API 구현
```

---

### PR 작성 원칙

- PR 템플릿 준수
- 자동 종료 설정

```
closed: #12
```

- 변경 라인 400줄 이하 권장

### Merge 기준

- 리뷰 1명 이상 승인

# 5. Merge

### 기본 전략

```
feature → develop → main
```

### 절차

1. feat 브랜치 → develop PR
2. 리뷰 후 merge

# 6. 협업 가이드 (충돌 방지)

### 최신 상태 유지

작업 전 항상 develop 동기화

```bash
git checkout develop
git pull origin develop
```

```bash
git checkout feat/branch-name
git merge develop

```

# 7. 파일 관리 주의사항

### 설정 파일

- `application.yml`
- `.env`

변경 시 반드시 공유

# **8. hotfix vs. release**

- `hotfix`는 `main` 에서 땀
- `release`는 `develop` 에서 땀
- 결국에 둘다 `develop` 이랑 `main`에 병합되어야 함