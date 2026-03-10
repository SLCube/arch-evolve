package com.playground.delivery.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.playground.delivery.domain.outbox.OutboxEventType
import com.playground.delivery.domain.outbox.OutboxStatus
import com.playground.delivery.fixture.application.domain.DeliveryDomainTestFixture
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class OutboxFactoryTest {
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    private val outboxFactory = OutboxFactory(objectMapper)

    @Test
    fun `deliveryCreated 호출 시 DELIVERY_CREATED 타입 PENDING Outbox가 생성되어야 한다`() {
        // given
        val delivery =
            DeliveryDomainTestFixture.mockDelivery(
                id = 1L,
                orderId = 100L,
                userId = 2L,
            )

        // when
        val outbox = outboxFactory.deliveryCreated(delivery)

        // then
        outbox.eventType shouldBe OutboxEventType.DELIVERY_CREATED
        outbox.status shouldBe OutboxStatus.PENDING
        outbox.orderId shouldBe delivery.orderId

        val payloadTree = objectMapper.readTree(outbox.payload)
        payloadTree["deliveryId"].longValue() shouldBe delivery.id
        payloadTree["orderId"].longValue() shouldBe delivery.orderId
        payloadTree["userId"].longValue() shouldBe delivery.userId
        payloadTree["eventId"].asText() shouldBe outbox.eventId.toString()
        payloadTree["occurredAt"].asText() shouldBe outbox.occurredAt.toString()
    }
}
