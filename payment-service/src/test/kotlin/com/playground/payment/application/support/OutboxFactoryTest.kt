package com.playground.payment.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.playground.payment.domain.event.PaymentAuthorizedEvent
import com.playground.payment.domain.event.PaymentFailedEvent
import com.playground.payment.domain.outbox.OutboxEventType
import com.playground.payment.domain.outbox.OutboxStatus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
class OutboxFactoryTest {
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    private val outboxFactory = OutboxFactory(objectMapper)

    @BeforeEach
    fun setUp() {
        MDC.put("requestId", "test-request-id")
    }

    @AfterEach
    fun tearDown() {
        MDC.clear()
    }

    @Test
    fun `PaymentAuthorizedEvent를 Outbox로 변환하면 PAYMENT_AUTHORIZED 타입으로 생성되어야 한다`() {
        // given
        val occurredAt = LocalDateTime.now()
        val event =
            PaymentAuthorizedEvent(
                eventId = UUID.randomUUID(),
                orderId = 100L,
                userId = 1L,
                amount = BigDecimal("10000"),
                pgTransactionId = "PG-TX-001",
                occurredAt = occurredAt,
            )

        // when
        val outbox = outboxFactory.from(event)

        // then
        outbox.eventType shouldBe OutboxEventType.PAYMENT_AUTHORIZED
        outbox.status shouldBe OutboxStatus.PENDING
        outbox.eventId shouldBe event.eventId
        outbox.occurredAt shouldBe event.occurredAt
        outbox.requestId shouldBe "test-request-id"

        val payloadTree = objectMapper.readTree(outbox.payload)
        payloadTree["orderId"].longValue() shouldBe event.orderId
        payloadTree["userId"].longValue() shouldBe event.userId
        payloadTree["pgTransactionId"].textValue() shouldBe event.pgTransactionId
        payloadTree["amount"].decimalValue() shouldBe event.amount
    }

    @Test
    fun `PaymentFailedEvent를 Outbox로 변환하면 PAYMENT_FAILED 타입으로 생성되어야 한다`() {
        // given
        val event =
            PaymentFailedEvent(
                eventId = UUID.randomUUID(),
                orderId = 200L,
                userId = 2L,
                failReason = "한도 초과",
                occurredAt = LocalDateTime.now(),
            )

        // when
        val outbox = outboxFactory.from(event)

        // then
        outbox.eventType shouldBe OutboxEventType.PAYMENT_FAILED
        outbox.status shouldBe OutboxStatus.PENDING

        val payloadTree = objectMapper.readTree(outbox.payload)
        payloadTree["failReason"].asText() shouldBe event.failReason
        payloadTree["orderId"].asLong() shouldBe event.orderId
        payloadTree["userId"].asLong() shouldBe event.userId
    }
}
