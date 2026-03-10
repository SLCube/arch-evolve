package com.playground.delivery.persistence.adapter

import com.playground.delivery.domain.outbox.OutboxEventType
import com.playground.delivery.domain.outbox.OutboxStatus
import com.playground.delivery.persistence.entity.DeliveryEventOutboxJpaEntity
import com.playground.delivery.persistence.repository.DeliveryEventOutboxRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(OutboxQueryAdapter::class)
class OutboxQueryAdapterTest(
    @param:Autowired private val outboxQueryAdapter: OutboxQueryAdapter,
    @param:Autowired private val deliveryEventOutboxRepository: DeliveryEventOutboxRepository,
) {
    @Test
    fun `findByStatus 호출 시 PENDING 상태 이벤트만 반환해야 한다`() {
        // given
        saveOutboxEntity(orderId = 100L, status = OutboxStatus.PENDING)
        saveOutboxEntity(orderId = 101L, status = OutboxStatus.PENDING)
        saveOutboxEntity(orderId = 102L, status = OutboxStatus.PUBLISHED)

        // when
        val result = outboxQueryAdapter.findByStatus(OutboxStatus.PENDING, 10)

        // then
        result.size shouldBe 2
        result.all { it.status == OutboxStatus.PENDING } shouldBe true
    }

    @Test
    fun `findByStatus 호출 시 PENDING 이벤트가 없으면 빈 리스트를 반환해야 한다`() {
        // given
        saveOutboxEntity(orderId = 200L, status = OutboxStatus.PUBLISHED)

        // when
        val result = outboxQueryAdapter.findByStatus(OutboxStatus.PENDING, 10)

        // then
        result.size shouldBe 0
    }

    @Test
    fun `findByStatus 호출 시 limit 개수만큼만 반환해야 한다`() {
        // given
        repeat(5) { i ->
            saveOutboxEntity(orderId = (300L + i), status = OutboxStatus.PENDING)
        }

        // when
        val result = outboxQueryAdapter.findByStatus(OutboxStatus.PENDING, 3)

        // then
        result.size shouldBe 3
    }

    private fun saveOutboxEntity(
        orderId: Long,
        status: OutboxStatus,
    ) {
        deliveryEventOutboxRepository.save(
            DeliveryEventOutboxJpaEntity(
                eventId = UUID.randomUUID(),
                orderId = orderId,
                eventType = OutboxEventType.DELIVERY_CREATED,
                payload = """{"orderId": $orderId}""",
                status = status,
                occurredAt = LocalDateTime.now(),
            ),
        )
    }
}
