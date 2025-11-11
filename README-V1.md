# PlayGround Project (V1: Layered Monolith)

## 프로젝트 소개

`PlayGround`는 아키텍처의 점진적인 진화 과정을 담아내는 것을 목표로 하는 토이 프로젝트입니다. 현재 버전(V1)은 기본적인 계층형 모놀리식 아키텍처를 기반으로 사용자, 상품, 주문 관리 기능을 구현하고 있습니다.

이 프로젝트는 단순히 기능을 구현하는 것을 넘어, **의도적인 기술 부채를 만들고 이를 식별, 관리, 해결해나가는 과정**을 통해 아키텍처가 어떻게 개선되고 발전하는지를 보여주는 데 중점을 둡니다.

## V1 아키텍처: 계층형 모놀리식 (Layered Monolith)

현재 프로젝트는 전통적인 계층형 아키텍처를 따르고 있습니다.

```
+---------------------+
|   Controller Layer  |  (Web)
+---------------------+
          |
          v
+---------------------+
|    Service Layer    |  (Business Logic)
+---------------------+
          |
          v
+---------------------+
|   Repository Layer  |  (Persistence)
+---------------------+
          |
          v
+---------------------+
|    Domain Layer     |  (JPA Entities)
+---------------------+
```

- **Controller Layer:** 클라이언트의 요청을 받아 Service Layer로 전달하고 응답을 반환합니다. 인증/인가 처리 및 요청 DTO 유효성 검증을 담당합니다.
- **Service Layer:** 비즈니스 로직을 처리하고 트랜잭션을 관리합니다. 여러 Repository를 조합하여 상위 수준의 비즈니스 흐름을 정의합니다.
- **Repository Layer:** 데이터베이스 접근을 담당하며, Spring Data JPA를 활용합니다.
- **Domain Layer:** 핵심 비즈니스 엔티티(JPA Entity)와 그 엔티티가 가지는 비즈니스 로직을 포함합니다.

**특징:**
- JPA Entity가 도메인 엔티티의 역할을 겸하고 있습니다. (Fat JPA Domain의 가능성)
- Service Layer가 다른 Service Layer의 메소드를 직접 호출하여 비즈니스 로직을 오케스트레이션합니다. (강한 결합의 가능성)

## 주요 설계 결정

### 테스트 프레임워크 (`performAndDocument` DSL)

이 프로젝트는 테스트 코드의 가독성, 일관성, 유지보수성을 높이기 위해 `performAndDocument`라는 커스텀 DSL(Domain-Specific Language) 기반의 테스트 프레임워크를 구축했습니다.

- **선언적 API 테스트:** `httpMethod`, `urlTemplate`, `requestBody`, `expectedStatus` 등 테스트의 의도를 명확하게 드러내는 선언적인 방식으로 API 테스트를 작성할 수 있습니다.
- **문서화 통합:** 하나의 테스트 코드로 API 테스트와 Spring REST Docs를 이용한 문서 생성을 동시에 해결합니다. 이를 통해 테스트와 문서 간의 불일치를 원천적으로 방지하고, 항상 최신 상태의 API 문서를 유지할 수 있습니다.

```kotlin
// 예시: performAndDocument DSL 사용법
performAndDocument("주문 생성 - 성공") {
    httpMethod = HttpMethod.POST
    urlTemplate = "/orders"
    requestBody = orderRequest
    accessToken = jwtToken
    expectedStatus = status().isCreated
    // ...
}
```

## 주요 기능

### 사용자 관리
- **회원가입:** 새로운 사용자를 등록합니다. (POST /users/sign-up)
- **로그인:** 사용자 인증 후 JWT 토큰을 발급합니다. (POST /users/login)
- **닉네임 변경:** 사용자의 닉네임을 변경합니다. (PATCH /users/{userId}/nickname)
- **비밀번호 변경:** 사용자의 비밀번호를 변경합니다. (PATCH /users/{userId}/password)

### 상품 관리
- **상품 등록:** 관리자 권한으로 새로운 상품을 등록합니다. (POST /products)
- **상품 조회:** 특정 상품 또는 전체 상품 목록을 조회합니다. (GET /products/{id}, GET /products)
- **상품 수정:** 관리자 권한으로 상품 정보를 수정합니다. (PATCH /products/{id})
- **재고 차감:** 관리자 권한으로 상품 재고를 차감합니다. (POST /products/{id}/decrease-stock)

### 주문 관리
- **주문 생성:** 로그인한 사용자가 상품을 주문합니다. (POST /orders)

##  기술 스택

- **언어:** Kotlin
- **프레임워크:** Spring Boot 3.3.1
- **데이터베이스:** H2 Database (인메모리)
- **ORM:** Spring Data JPA
- **보안:** Spring Security, JWT
- **테스트:** Kotest, JUnit5, Spring Rest Docs
- **빌드:** Gradle (Kotlin DSL)

## 향후 계획 (V1.5: Hexagonal/Clean Architecture 전환)

현재 V1 아키텍처에서 인지된 'Fat JPA Domain'과 계층 간의 강한 결합 문제를 해결하기 위해, 다음 버전(V1.5)에서는 **Hexagonal Architecture (Ports & Adapters) 및 Clean Architecture** 원칙을 적용하여 애플리케이션의 내부 구조를 재설계할 예정입니다. 이 과정에서 도메인 계층과 인프라 계층의 분리, 의존성 역전 원칙 적용 등을 통해 더욱 유연하고 유지보수하기 쉬운 아키텍처로 진화할 것입니다.
