# PlayGround Project

> 아키텍처의 점진적 진화 과정을 기록하는 프로젝트

## 프로젝트 소개

`PlayGround`는 단순히 기능을 구현하는 것을 넘어, **아키텍처가 어떻게 진화하는지, 그 과정에서 개발자가 어떤 판단을 내리는지**를 데이터와 함께 기록하는 프로젝트입니다.

### 핵심 가치

- **가설 → 실험 → 측정 → 결론**: 모든 아키텍처 결정을 데이터로 뒷받침
- **의도적인 기술 부채**: 문제를 만들고, 식별하고, 해결하는 과정을 투명하게 공개
- **실패도 포함**: 분산 락 실험 → 성능 악화 → 대안 선택 등 시행착오 기록
- **성능 측정**: 각 Phase별 Before/After를 수치와 그래프로 증명

## 기술 스택

### Backend
- **언어**: Kotlin
- **프레임워크**: Spring Boot 3.5.10
- **데이터베이스**: PostgreSQL
- **캐시**: Redis
- **ORM**: Spring Data JPA
- **보안**: Spring Security, JWT

### 테스트 & 품질
- **테스트**: Kotest, JUnit5, Spring REST Docs
- **코드 커버리지**: JaCoCo
- **코드 스타일**: Ktlint

### 인프라 & 모니터링
- **컨테이너**: Docker
- **부하 테스트**: k6
- **메트릭**: Grafana + Prometheus
- **로그**: Loki

### 빌드
- **빌드 도구**: Gradle (Kotlin DSL)

## 아키텍처 로드맵

```
v1.0  → Single Module Layered Architecture
          └─ 의도적 기술 부채: Fat JPA Domain, Service 간 강한 결합

v1.5  → Single Module Hexagonal Architecture
          └─ Ports & Adapters, 도메인 순수성 확보, CQS 패턴

v2.0  → Multi Module Hexagonal Architecture
          └─ Contract Module, 도메인 경계 컴파일 타임 강제

v2.5  → High Performed Monolith (현재)
          └─ Redis 재고 관리 (P95 94.9%↓), 비동기 이벤트 (P95 99.8%↓)
          └─ 모놀리스 한계 확인: Connection Pool, 리소스 격리 불가

v3.0  → Choreography Microservices Architecture (예정)
          └─ 독립 DB, 리소스 격리, Saga 패턴, 분산 트랜잭션 처리
```

## 공통 테스트 도구

### `performAndDocument` DSL

이 프로젝트는 테스트 코드의 가독성, 일관성, 유지보수성을 높이기 위해 `performAndDocument`라는 커스텀 DSL(Domain-Specific Language) 기반의 테스트 프레임워크를 구축했습니다.

**특징:**
- **선언적 API 테스트**: `httpMethod`, `urlTemplate`, `requestBody`, `expectedStatus` 등 테스트의 의도를 명확하게 표현
- **문서화 통합**: 하나의 테스트 코드로 API 테스트와 Spring REST Docs 문서 생성을 동시에 해결
- **일관성**: 모든 버전에서 동일한 테스트 방식 적용

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

## 각 버전 상세 문서

각 Phase별 상세한 아키텍처 결정, 문제 인식, 해결 과정은 아래 문서를 참고하세요:

- **[V1.0: Layered Monolith](./README-V1.md)**
  전통적인 계층형 아키텍처, 의도적 기술 부채 생성

- **[V1.5: Hexagonal Architecture](./README-V1.5.md)**
  Ports & Adapters, 도메인 순수성, CQS 패턴

- **[V2.0: Multi Module Hexagonal Architecture](./README-V2.0.md)**
  Contract Module, 도메인 경계 컴파일 타임 강제

- **[V2.5: High Performed Monolith](./README-V2.5.md)** (작성 예정)
  Redis 재고 관리, 비동기 이벤트, 모놀리스 한계 확인

## 브랜치 구조

```
phase/1.0-single-layered-architecture          (V1.0)
phase/1.5-single-module-hexagonal-architecture (V1.5)
phase/2.0-multi-module-hexagonal-architecture  (V2.0)
phase/2.5-high-performed-monolith              (V2.5, 현재)
phase/3.0-choreography-msa                     (V3.0, 예정)
```

각 브랜치는 해당 Phase의 완성된 코드를 포함하며, 독립적으로 실행 가능합니다.