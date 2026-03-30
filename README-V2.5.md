# PlayGround Project (V2.5: High Performed Monolith)

## V2.5 아키텍처: 고성능 모놀리스

V2.5는 V2.0에서 다져진 안정된 멀티 모듈 구조 위에서, **실제 트래픽 환경에서의 성능 병목을 식별하고 개선**하는 단계입니다.

구조적 안정성을 넘어, 측정 → 개선 → 검증의 과학적 방법론을 통해 고성능 모놀리스를 구축하고, 동시에 **모놀리스의 근본적 한계**를 데이터로 증명하여 MSA 전환의 명분을 확보합니다.

**핵심 원칙:**
- **가설 → 실험 → 측정 → 결론**: 모든 개선을 데이터로 뒷받침
- **실패 포함**: 잘못된 가설(동기식 AFTER_COMMIT)도 기록하여 실제 학습 과정 공유
- **성능 측정**: Before/After를 수치와 그래프로 증명
- **한계 인정**: 모놀리스로 해결 불가능한 문제 명확히 식별

## 주요 변경 사항

V2.5는 4개의 Phase를 거쳐 점진적으로 진화했습니다.

### Phase 1: 성능 병목 식별 및 문제 인식

#### 1-1. 부하 테스트 환경 구축

- k6 부하 테스트 도구 도입
- Grafana + Prometheus 메트릭 수집
- Loki 로그 집계

**로컬 테스트 환경:**
- MacBook Air (M1, 8 Core, 16 GB)

#### 1-2. 초기 성능 측정 및 가설 수립

**문제 인식:**

동기식으로 결제 로직(Payment → Order → Product → Delivery)을 실행했을 때, vu1과 vu5의 응답 속도가 약 5배 차이 발생.

**가설:**

결제 로직에 심각한 병목이 존재한다.

**측정 결과:**

![결제로직 동작 vu1](./images/v2.5-phases/00-initial-problem/결제로직%20동작%20vu1%20rps1%2060초.png)
*결제 로직 포함 (vu1): P95 627ms*

![결제로직 동작 vu5](./images/v2.5-phases/00-initial-problem/결제로직%20동작%20vu5%20rps5%2060초.png)
*결제 로직 포함 (vu5): P95 3.63초*

| VU | RPS | P95 | P99 | 배수 |
|----|-----|-----|-----|------|
| 1  | 1   | 627ms | 759ms | 1x |
| 5  | 5   | **3.63s** | **5.39s** | **5.8x** |

**분석:**
- vu5에서 P95가 3.63초 (vu1 대비 약 5.8배)
- 단순히 요청이 5배 증가했는데, 응답 시간도 5배 이상 증가
- 동기 처리로 인한 누적 지연 의심

**결론:**

결제 로직 제거 후 재측정하여 가설 검증 필요.

#### 1-3. 가설 검증 (결제 로직 제거)

**실험:**

Event consumer를 비활성화하여 결제 로직(Payment → Order → Product → Delivery) 제거 후 재측정.

**측정 결과:**

![결제로직 미동작 vu1](./images/v2.5-phases/00-initial-problem/결제로직%20미동작%20vu1%20rps1%2030초.png)
*결제 로직 제외 (vu1): P95 59.3ms*

![결제로직 미동작 vu5](./images/v2.5-phases/00-initial-problem/결제로직%20미동작%20vu5%20rps5%20180초.png)
*결제 로직 제외 (vu5): P95 40.3ms*

| 상태 | RPS | P95 | P99 | 감소율 |
|------|-----|-----|-----|--------|
| 결제 **동작** | 5 | 3.63s | 5.39s | - |
| 결제 **미동작** | 5 | **40.3ms** | **48.4ms** | **98.9%** |

**결론:**

✅ **가설이 맞았다!** 결제 로직 제거 시 P95가 3.63초 → 40.3ms로 **90배 감소**.

동기식 결제 로직이 주요 병목임을 데이터로 증명.

#### 1-4. 재고 차감 로직의 성능 한계 탐색

**새로운 질문:**

결제 로직 없이 재고 차감만으로 얼마나 확장 가능한가?

**실험:**

재고 차감만 남긴 상태에서 RPS를 점진적으로 증가시키며 병목 지점 탐색.

**측정 결과:**

![재고차감 RPS 20](./images/v2.5-phases/00-initial-problem/결제로직%20미동작%20vu20%20rps20%20180s.png)
*RPS 20: P95 27.6ms (정상)*

![재고차감 RPS 700](./images/v2.5-phases/00-initial-problem/결제로직%20미동작%20vu700%20rps700%20180s.png)
*RPS 700: P95 1.41초 (병목 발생)*

| RPS | P95 | P99 | 상태 |
|-----|-----|-----|------|
| 20  | 27.6ms | 34.8ms | ✅ 정상 |
| 700 | **1.41s** | **1.57s** | ⚠️ 병목 |

**Connection Pool 분석 (RPS 700):**

![Connection Pool Statistics](./images/v2.5-phases/00-initial-problem/결제로직%20미동작%20vu700%20rps700%20180s%20connection.png)

**HikariCP 상태:**
- Pool Size: 10
- Active: 평균 9.23, 최대 10 (거의 100% 사용)
- **Pending: 평균 174, 최대 189** 🚨

**결론:**

RPS 700에서 새로운 병목 발견:
- Connection Pool 부족 (Pending 대기 평균 174개)
- 재고 차감 로직 자체에도 최적화 필요

**개선 방향:**
1. 재고 관리를 Redis로 이관 (Disk I/O 감소)

### Phase 2: Redis 재고 관리

#### 2-1. Redis 도입 배경

**첫 번째 시도: Connection Pool 증가**

RPS 700에서 Connection Pool Pending이 평균 174개 발생 → Pool Size를 10에서 50으로 증가 시도.

**결과:**
- Pending은 줄었지만 근본적 해결 안 됨
- PostgreSQL max_connections 한계 존재
- 단순히 Connection을 늘리는 것은 임시방편

**고민:**

재고라는 데이터가 굳이 **실시간으로 DB에 반영**될 필요가 있을까?

**핵심 인사이트:**

주문이 들어올 때마다 PostgreSQL에 UPDATE 쿼리를 날리는 것이 병목.
→ Disk I/O가 누적되어 Connection 점유 시간 증가
→ Connection Pool Pending 발생

**해결 아이디어:**

주기적으로 DB에 반영해서 Disk I/O를 줄이자.
- 실시간 반영 대신 **배치 동기화** (예: 10초마다)
- Disk I/O 횟수 감소 → Connection 점유 시간 감소

**새로운 문제:**

그 사이에 어딘가에선 **실시간으로 재고를 계산**해야 함.
- 주문 요청 시 재고가 있는지 즉시 확인 필요
- DB는 10초마다 동기화 → 실시간 조회 불가능

**최종 해결책:**

메모리에 재고를 캐싱하는 **Redis** 도입.
- In-Memory 저장소로 Disk I/O 완전 제거
- 실시간 재고 조회/차감 (INCR, DECR 연산)
- 주기적으로 PostgreSQL과 동기화 (배치)

**Redis 선택 이유:**
- In-Memory 저장소 → Disk I/O 없음
- 원자적 연산 지원 (INCR, DECR) → 동시성 안전
- Lua Script로 복잡한 재고 로직 구현 가능

#### 2-2. 성능 개선 결과

**Before (Phase 1: PostgreSQL 재고 관리, RPS 700):**

![Before Connection Pool](./images/v2.5-phases/00-initial-problem/결제로직%20미동작%20vu700%20rps700%20180s%20connection.png)
- Pool Size: 10
- Pending: 평균 174, 최대 189

**After (Redis 재고 관리, RPS 700):**

![After Performance](./images/v2.5-phases/01-phase1-redis/redis재고관리%20p95%20p99.png)

![After Connection Pool](./images/v2.5-phases/01-phase1-redis/redis%20재고관리%20connection%20pool.png)
- Pool Size: 50
- Pending: **0** (완전 해소)

**성능 개선 (RPS 700 기준):**

| 항목 | Before | After | 개선율 |
|------|--------|-------|--------|
| **P95** | 1.41s | **88.8ms** | **93.7% ↓** (약 16배) |
| **P99** | 1.57s | **341ms** | **78.3% ↓** (약 5배) |
| **Connection Pool Pending** | 평균 174 | **0** | **완전 해소** |
| **Active Connections** | 평균 9.23 (거의 100%) | 평균 7.46 (15%) | 85% 감소 |

**핵심 성과:**
- Disk I/O 제거로 Connection 점유 시간 대폭 감소
- Pending 대기 완전 해소 (174 → 0)
- P95 응답 시간 93.7% 개선 (1.41s → 88.8ms)
- Connection Pool 여유 확보 (Active 15% 수준)

### Phase 3: 비동기 이벤트 처리

#### 3-1. 동기 처리의 문제

**현재 구조 (Phase 2 완료 후):**

주문 생성 시 Payment → Order → Product → Delivery가 **동기식**으로 실행.

**측정 결과 (RPS 100):**

![결제로직 개선전](./images/v2.5-phases/02-phase2-payment/결제로직%20개선전%20rps100%20p95%20p99.png)

- **P95**: 16.4s
- **P99**: 17.4s
- **에러율**: 52.2%

**문제 분석:**
- 응답 시간 = 주문 생성 + 결제 + 재고 + 배송 (모두 합산)
- 한 단계라도 지연되면 전체 응답 시간 증가
- 사용자는 불필요하게 오래 기다림 (주문 생성만 확인하면 되는데)

#### 3-2. 첫 번째 시도: 동기식 AFTER_COMMIT

**가설:**

`@TransactionalEventListener(phase = AFTER_COMMIT)`을 사용하면:
- 트랜잭션 커밋 후 Connection이 반환됨
- 다음 이벤트가 새로운 Connection을 얻어 처리됨
- 응답 시간 감소 예상

**구현:**
```kotlin
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
fun handleOrderCreated(event: OrderCreatedEvent) {
    // 결제, 재고, 배송 처리
}
```

**측정 결과:**

![동기식 AFTER_COMMIT](./images/v2.5-phases/02-phase2-payment/결제요청%20after%20commit이후%20p95%20p99.png)

- **P95**: 6.65s
- **P99**: 6.98s
- **에러율**: 60.8%

| 상태 | P95 | P99 | 에러율 |
|------|-----|-----|--------|
| Before | 16.4s | 17.4s | 52.2% |
| AFTER_COMMIT | 6.65s | 6.98s | 60.8% |

**결론:**

❌ **가설이 틀렸다!**

- Connection은 빨리 반환되지만, **HTTP 응답은 여전히 대기**
- `AFTER_COMMIT`은 트랜잭션 커밋 후 실행하지만, **여전히 동기적으로 실행됨** (같은 스레드)
- 응답 시간 = 주문 + 결제 + 재고 + 배송 (여전히 합산)
- 6.65초는 여전히 너무 느림

**깨달음:**

트랜잭션 경계와 스레드 실행은 별개다.
- `AFTER_COMMIT`: 트랜잭션 커밋 후 실행 (Connection 반환)
- **하지만 같은 스레드**에서 실행 → HTTP 응답 대기

#### 3-3. 두 번째 시도: 비동기식 AFTER_COMMIT

**새로운 가설:**

`@Async`를 추가하면:
- 이벤트 리스너가 **별도 스레드**에서 실행
- 주문 생성 완료 후 즉시 HTTP 응답 반환
- 결제, 재고, 배송은 백그라운드 처리

**구현:**
```kotlin
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
fun handleOrderCreated(event: OrderCreatedEvent) {
    // 별도 스레드에서 결제, 재고, 배송 처리
}
```

**측정 결과:**

![비동기식 AFTER_COMMIT](./images/v2.5-phases/02-phase2-payment/async%20적용%20후%20rps100%20p95%20p99.png)

- **P95**: 13.8ms
- **P99**: 34.1ms
- **에러율**: 0% (에러 없음)

#### 3-4. 성능 개선 결과

**전체 비교:**

| 단계 | P95 | P99 | 에러율 |
|------|-----|-----|--------|
| **Before** (동기식) | 16.4s | 17.4s | 52.2% |
| **동기 AFTER_COMMIT** | 6.65s | 6.98s | 60.8% |
| **비동기 AFTER_COMMIT** | **13.8ms** | **34.1ms** | **0%** |

**핵심 성과:**
- ✅ P95 응답 시간 **99.8% 개선** (16.4s → 13.8ms)
- ✅ P99 응답 시간 **99.8% 개선** (17.4s → 34.1ms)
- ✅ RPS 100 완벽 대응
- ✅ 사용자는 주문 생성만 기다림 (결제, 재고, 배송은 백그라운드)

**트랜잭션 설계:**
- 각 도메인의 트랜잭션 독립 실행
- `AFTER_COMMIT` + `@Async` 조합으로 성능 확보

**핵심 교훈:**
- `@TransactionalEventListener(AFTER_COMMIT)`: 트랜잭션 경계 제어
- `@Async`: 스레드 실행 제어
- 두 가지를 함께 사용해야 비동기 처리 완성

## 남은 질문들

비동기 처리로 성능은 해결했지만, 한 가지 의문이 남았다.

결제는 금전 거래가 오가는 도메인이다. `@Async`는 프로세스 장애 시 처리 중이던 이벤트를 보장하지 않는다. 결제가 실패해도 사용자는 이미 "주문 완료" 화면을 보고 있다.

그리고 더 근본적인 질문들이 있었다.

- 트래픽이 지금보다 더 늘어난다면?
- 결제 도메인에 장애가 발생하면 주문 도메인까지 영향을 받아야 할까?
- 모든 도메인이 단일 DB를 공유하는 한, 이 문제들은 해결되지 않는다.

→ **[V3.0: Microservices Architecture](./README-V3.0.md)**

