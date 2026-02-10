# Comments & Formatting

## 📝 주석 규칙

- 코드로 설명 가능한 것은 주석을 달지 않는다
- **“Why(왜)”**에 집중한다. 코드를 **왜** 그렇게 짰는지 문맥 필요
- **TODO** : 추후에 구현해야 하거나 리팩토링이 필요한 부분에는 반드시 `// TODO: 설명`을 남긴다

## 🔧 포맷팅

- IDE 설정 통일
- 들여쓰기 : **4 Spaces** 사용

---

# Java Code Convention

## ✏️ 네이밍 규칙

변수와 클래스 이름은 그 자체로 주석이 되도록 명확하게 작성

### 기본 케이스 Case Styles

- **PascalCase** : 클래스명, 인터페이스명
- **camelCase** : 메서드명, 변수명, 파라미터명
- **SCREAMING_SNAKE_CASE** : 상수
- **snake_case** : 데이터베이스 테이블 및 컬럼명

### 상세 규칙

- **클래스**

  명사로 작성 - `UserController` ⭕ / UserControl ❌

- **메서드**

  동사로 시작

    - `createUser()` ⭕ / userCreate() ❌
    - 조회 : `findBy…`, `getBy…`, `searchBy…`
    - 검사: `existsBy...`
    - 삭제: `deleteBy...`, `removeBy...`
- **Boolean 변수**

  is, has 등으로 시작해 의미를 명확히 함 - `isDeleted`, `hasToken`


## 💻 코딩 스타일 & 롬복

코드 안정성과 유지보수성

### 롬복 사용 규칙

- **@Setter** 사용 지양
    - 객체의 무분별한 변경을 막기 위해 Entity에는 절대 `@Setter` 를 사용하지 않음
    - 명확한 비즈니스 메서드를 생성(ex. changePassword()) or `Builder` 패턴 사용
- **@Data** 사용 금지
    - `@ToString`, `@EqualsAndHashCode` 등이 포함되어 무한 참조 등 예상치 못한 오류 유발
    - 필요한 어노테이션만 명시
- **생성자 주입 사용**

  : `@Authwired` 대신 `@RequiredArgsConstructor`와 `final` 필드를 사용해 의존성 주입받기


```java
✅ 예시
@Getter
@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {
    private final UserService userService; // final + RequiredArgsConstructor
}
```

### 로깅

- `System.out.println()` 사용 금지 → 성능 저하 및 관리 불가
- `@Slf4j`를 사용해 `log.info()`, `log.error()` 등으로 로그 남김

## 🏗️ 아키텍처 및 데이터 흐름

Architecture & Data Flow

### 계층형 구조

- Controller : 요청/응답 처리, 파라미터 검증 → 비즈니스 로직 금지
- Service : 비즈니스 로직 구현, 트랜잭션 관리
- Repository : DB 접근, 쿼리 수행

### DTO 규칙 - Entity 노출 금지

API 응답이나 요청 바디로 Entity 클래스 직접 사용 금지. 반드시 DTO로 변환해 통신

---

# Database Convention

## 🏷️ 테이블 및 컬럼

Table & Column

- 모든 테이블과 컬럼은 **snake_case** 사용
- 모든 이름은 **단수형 명사** 사용
- PK : `id` / FK : `_id`
- Boolean 컬럼 : `is_` 접두사 사용

## 📅 날짜 및 시간

- 데이터 생성 시간 : `created_at`
- 데이터 수정 시간 : `updated_at`
- 타입 : `DATETIME` 또는 `TIMESTAMP` 사용(Java에서 LocalDateTime 매핑)

---

# API & Error Handling

## 📢 공통 응답 포맷

모든 API는 아래 JSON 구조로 응답. success 필드로 성공 여부를 1차 판단

### ✅ **성공**

```json
{
  "success": true,
  "code": "OK",
  "message": "요청 성공",
  "data": {
      "userId": 1,
      "username": "username"
  }
}
```

### ❌ **실패(에러)**

```json
{
  "success": false,
  "code": "AUTH_001",
  "message": "로그인이 필요합니다",
  "errors": [
    // 유효성 검사 실패 시에만 포함 (Optional)
    { "field": "email", "reason": "이메일 형식이 아닙니다" }
  ]
}
```

### 📄 목록 조회 응답 (Pagination)

data 필드 내부에 페이지네이션 정보 포함

```json
{
  "success": true,
  "code": "OK",
  "message": "조회 성공",
  "data": {
    "items": [ ... ],
    "page": 1,
    "size": 20,
    "totalElements": 153,
    "totalPages": 8,
    "hasNext": true
  }
}
```

## 💬 상태 코드 및 에러 네이밍

Status Code & Naming

### HTTP 상태코드 규칙

| **Code** | **의미** | **설명** |
| --- | --- | --- |
| **200** | OK | 조회/수정/삭제 성공 |
| **201** | Created | 리소스 생성 성공 (Location 헤더 권장) |
| **204** | No Content | 데이터 없이 성공 (삭제 등) |
| **400** | Bad Request | 요청 파라미터/형식 오류 |
| **401** | Unauthorized | 인증 필요/토큰 만료 |
| **403** | Forbidden | 권한 없음 (접근 불가) |
| **404** | Not Found | 리소스 없음 |
| **409** | Conflict | 데이터 중복/충돌 |
| **500** | Internal Error | 서버 내부 오류 |

### **에러 코드 네이밍 룰**

**: 도메인_번호** 형태로 고정

- `AUTH_001` : 토큰 없음
- `AUTH_002` : 토큰 만료
- `USER_001` : 유저 없음
- `VALID_001` : 검증 실패
- `COMMON_001` : 알 수 없는 오류

## ⚠️ 에러 처리 구현 가이드

Implementation

### 패키지 구조

```
global
 └─ error
     ├─ GlobalExceptionHandler.java  (전역 예외 처리기)
     ├─ ErrorResponse.java           (공통 응답 DTO)
     └─ ErrorCode.java               (에러 코드 Enum)
 └─ exception
     └─ BusinessException.java       (커스텀 예외 상위 클래스)
```

### 핵심 규칙

1. Unchecked Exception 지향

   : 모든 커스텀 예외는 `RuntimeException`을 상속받아 트랜잭션 롤백이 가능하게 함

2. 중앙 집중 관리

   : 에러 메시지와 HTTP 상태 코드는 `ErrorCode` Enum에서 통합 관리(하드코딩 금지)

3. 일관된 응답 포맷 : 클라이언트는 항상 JSON 구조로 에러를 수신해야 한다

---

# Test Code Convention

## 🗒️ 테스트 구조

BDD 스타일을 차용해 3단계로 명확히 구분

```java
@Test
void 회원가입_성공() {
    // 1. Given (준비): 테스트에 필요한 데이터나 객체를 세팅
    UserDto request = new UserDto("khyun722", "password123");

    // 2. When (실행): 실제로 테스트할 메서드를 호출
    Long userId = userService.join(request);

    // 3. Then (검증): 결과가 예상과 일치하는지 단언(Assert)
    assertThat(userId).isNotNull();
}
```

## 📛 명명 규칙

- 테스트 메서드명은 한글로 작성해 어떤 시나리오를 검증하는지 직관적으로 알 수 있게 한다
- `메서드명_상황_예상결과` 또는 `기능_설명`

  login_WrongPassword_Fail() ❌ → `로그인_비밀번호_불일치_실패` ⭕


---

# Configuration & Security

## ⚙️ 설정 파일 분리

사고 방지와 개발 효율 목적

### 민감 정보 보호

DB 비밀번호, AWS Key, JWT Secret Key 등은 절대 Git에 올리지 않는다

### application.yaml 전략

- `application.yml`: 기본 설정 (Git 공유)
- `application-secret.yml`: 민감 정보 (Git Ignore 필수)
- 실행 시 `Dspring.profiles.active=secret` 등으로 포함

## 🌍 환경 변수 사용

로컬 개발 환경이나 배포 환경에서는 가능한 환경 변수를 사용해 값을 주입 받기