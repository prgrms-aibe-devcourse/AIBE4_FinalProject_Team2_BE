# 📌 QueryDSL 커스텀 리포지토리(Custom Repository) 사용 가이드

복잡한 동적 쿼리를 작성할 때 사용하는 QueryDSL 보일러플레이트 템플릿입니다. <br/>
아래 코드를 복사하여 `[Domain]` 부분을 본인이 담당하는 엔티티(Entity) 이름으로 변경하여 사용하세요.

---

## 1. QueryDSL 도입 배경
- 자바 코드 기반으로 쿼리를 작성해 컴파일 타임에 문법 오류를 사전에 차단하는 타입 안정성 확보 목적
- 서비스 전반에서 발생하는 복잡한 테이블 조인과 동적 쿼리를 직관적인 객체 지향 코드로 해결해 **코드 재사용성**과 **유지보수성 극대화**

## 2. Custom 인터페이스 (명세서)
- **파일 이름:** `[Domain]RepositoryCustom.java`
- **역할:** QueryDSL로 구현할 동적 쿼리 메서드의 이름과 반환 타입을 선언합니다.

```java
import java.util.List;

public interface [Domain]RepositoryCustom {
    // TODO: QueryDSL로 작성할 동적 쿼리 메서드 선언
    // 예시: List<[Domain]> search[Domain]s(String keyword);
}
```

## 3. Impl 구현체 (실제 쿼리 작성)
- **파일 이름:** `[Domain]RepositoryImpl.java`
- **역할:** Custom 인터페이스를 상속받아 `JPAQueryFactory`를 이용해 실제 QueryDSL 코드를 작성합니다.
- ⚠️ **주의:** 파일 이름은 반드시 `인터페이스명 + Impl` 형태를 지켜야 합니다.

```java
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

// TODO: 본인 도메인의 Q-Class 정적 임포트 (필수)
// import static com.aibe.team2.global.domain.Q[Domain].[domain];

@Repository
@RequiredArgsConstructor
public class [Domain]RepositoryImpl implements [Domain]RepositoryCustom {

    // 공통 환경 설정에서 등록한 빈(Bean)을 주입받아 사용합니다.
    private final JPAQueryFactory queryFactory;

    // TODO: Custom 인터페이스에 선언한 메서드 구현
    /* 예시 코드
    @Override
    public List<[Domain]> search[Domain]s(String keyword) {
        return queryFactory
                .selectFrom([domain])
                .where(
                        keywordContains(keyword) // 동적 쿼리 조건
                )
                .fetch();
    }

    // 동적 쿼리를 위한 BooleanExpression 메서드 분리 (null 반환 시 조건 무시됨)
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? [domain].title.contains(keyword) : null;
    }
    */
}
```

## 4. 기본 Repository (통합 인터페이스)
- **파일 이름:** `[Domain]Repository.java`
- **역할:** `JpaRepository`와 우리가 만든 `Custom` 인터페이스를 다중 상속받아 최종적으로 서비스(Service) 계층에서 호출하는 인터페이스입니다.

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface [Domain]Repository extends JpaRepository<[Domain], Long>, [Domain]RepositoryCustom {
    // Spring Data JPA 기본 메서드(save, findById 등)와
    // QueryDSL로 구현한 Custom 메서드를 모두 여기서 호출 가능합니다.
}
```