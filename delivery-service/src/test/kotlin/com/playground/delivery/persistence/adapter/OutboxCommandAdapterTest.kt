package com.playground.delivery.persistence.adapter

import com.playground.delivery.domain.outbox.DeliveryEventOutbox
import com.playground.delivery.domain.outbox.OutboxEventType
import com.playground.delivery.domain.outbox.OutboxStatus
import com.playground.delivery.persistence.repository.DeliveryEventOutboxRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(OutboxCommandAdapter::class)
class OutboxCommandAdapterTest(
    @param:Autowired private val outboxCommandAdapter: OutboxCommandAdapter,
    @param:Autowired private val deliveryEventOutboxRepository: DeliveryEventOutboxRepository,
    @param:Autowired private val testEntityManager: TestEntityManager,
) {
    @Test
    fun `save 호출 시 OutboxEvent를 DB에 저장하고 ID가 할당된 객체를 반환해야 한다`() {
        // given
        val outbox =
            DeliveryEventOutbox(
                eventId = UUID.randomUUID(),
                orderId = 100L,
                eventType = OutboxEventType.DELIVERY_CREATED,
                payload = """{"orderId": 100}""",
                status = OutboxStatus.PENDING,
                occurredAt = LocalDateTime.now(),
            )

        // when
        val saved = outboxCommandAdapter.save(outbox)

        // then
        saved.id shouldNotBe null
        saved.orderId shouldBe outbox.orderId
        saved.eventType shouldBe OutboxEventType.DELIVERY_CREATED
        saved.status shouldBe OutboxStatus.PENDING
    }

    @Test
    fun `bulkMarkAsPublished 호출 시 해당 ID들의 상태가 PUBLISHED로 변경되어야 한다`() {
        // given
        val outbox1 =
            DeliveryEventOutbox(
                eventId = UUID.randomUUID(),
                orderId = 200L,
                eventType = OutboxEventType.DELIVERY_CREATED,
                payload = """{"orderId": 200}""",
                status = OutboxStatus.PENDING,
                occurredAt = LocalDateTime.now(),
            )
        val outbox2 =
            DeliveryEventOutbox(
                eventId = UUID.randomUUID(),
                orderId = 201L,
                eventType = OutboxEventType.DELIVERY_CREATED,
                payload = """{"orderId": 201}""",
                status = OutboxStatus.PENDING,
                occurredAt = LocalDateTime.now(),
            )
        val saved1 = outboxCommandAdapter.save(outbox1)
        val saved2 = outboxCommandAdapter.save(outbox2)

        // when
        outboxCommandAdapter.bulkMarkAsPublished(listOf(saved1.id!!, saved2.id!!))
        testEntityManager.flush()
        testEntityManager.clear()

        // then
        val entity1 = deliveryEventOutboxRepository.findById(saved1.id!!).orElse(null)
        val entity2 = deliveryEventOutboxRepository.findById(saved2.id!!).orElse(null)
        entity1.status shouldBe OutboxStatus.PUBLISHED
        entity2.status shouldBe OutboxStatus.PUBLISHED
    }
}
