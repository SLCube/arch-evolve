# PlayGround Project (V3.0: Microservices)

## V3.0 아키텍처: 마이크로서비스

V3.0은 V2.5에서 데이터로 증명한 모놀리스의 한계(단일 DB 공유, 리소스 격리 불가)를 해결하기 위해 서비스를 분리하는 단계입니다.

그런데 MSA 전환 이후 부하 테스트를 진행하던 중, 아키텍처와는 무관하게 **V2.5 시절부터 잠재되어 있던 문제들**을 뒤늦게 발견했습니다. 두 가지 모두 MSA 전환 전에도 충분히 해결할 수 있었던 문제였습니다.

### 1. Kafka Consumer 처리량 문제와 시행착오

payment-service를 분리하고 처음 부하 테스트를 돌렸을 때, `order-created` 토픽에 이벤트가 빠르게 쌓이는 것을 확인했습니다. consumer가 발행 속도를 따라가지 못하는 lag 문제였습니다.

**첫 번째 시도: batch consume + bulk insert/update**

당시 Kafka partition 개념이 제대로 잡혀 있지 않았습니다. partition을 1~3개 수준으로 유지한 채, 여러 메시지를 모아 한 번에 처리하면 처리량이 늘어날 것이라 생각했습니다.

```
consumer가 메시지 N개를 한 번에 consume
  → 결제 N건을 bulk로 처리
  → DB insert/update도 batch로
  → 처리량 증가 기대
```

하지만 결제 도메인의 특성 앞에서 바로 막혔습니다. N건 중 일부만 결제에 실패했을 때 롤백 단위가 불명확해집니다. 성공한 건은 커밋하고 실패한 건만 재처리해야 하는데, 이 로직이 급격히 복잡해졌습니다. 결제는 건별 트랜잭션 보장이 필요한 도메인이었습니다.

**올바른 해결: partition 증가**

batch 접근을 폐기하고 방향을 바꿨습니다. partition을 50개로 늘리고 consumer concurrency를 동일하게 맞추는 방식입니다. 메시지 처리 방식을 바꾸는 게 아니라, 병렬로 처리하는 consumer 수를 늘리는 것이 핵심이었습니다.

```kotlin
// KafkaTopicConfig.kt (monolith)
TopicBuilder
    .name(KafkaProducerTopic.ORDER_CREATED)
    .partitions(50)  // 50개 partition
    .replicas(1)
    .build()

// KafkaConsumerConfig.kt (payment-service)
factory.setConcurrency(50)  // partition 수와 일치
```

partition 수 = consumer concurrency = 50. 50개 스레드가 각자 할당된 partition에서 독립적으로 메시지를 가져와 처리하므로 lag이 해소됐습니다. 각 메시지는 여전히 독립적인 트랜잭션으로 처리됩니다.

concurrency 값은 Little's Law로 산출했습니다.

```
필요 스레드 수 = 목표 RPS × 메시지 처리 시간(초)
             = 100 RPS × 0.4s (결제 게이트웨이 응답 시간)
             = 40
```

여유분을 포함해 50으로 설정했습니다.

### 2. DB 인덱스 부재

V2.5에서 RPS 100 수준의 테스트에서는 Full Scan이 눈에 띄는 병목으로 나타나지 않았습니다. V3.0에서 Kafka consumer들이 DB를 추가로 조회하는 구조가 되면서 3개 테이블의 인덱스 부재가 심각한 병목으로 드러났습니다.

| 테이블 | 추가된 인덱스 |
|--------|--------------|
| `payments` | `order_id` |
| `order_products` | `order_id` |
| `payment_methods` | `user_id` |

인덱스 3개 추가만으로 80 RPS 기준 P95가 1.03s → 592ms로 **42% 개선**되었고, monolith DB pool pending이 58 → 0으로 완전히 해소됐습니다. V2.5에서도 인덱스를 추가했다면 더 높은 RPS를 달성할 수 있었을 것입니다.

---
