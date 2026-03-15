package com.playground.payment.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.playground.payment.domain.event.PaymentAuthorizedEvent
import com.playground.payment.domain.event.PaymentFailedEvent
import com.playground.payment.domain.outbox.OutboxEventType
import com.playground.payment.domain.outbox.OutboxStatus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micrometer.tracing.Span
import io.micrometer.tracing.TraceContext
import io.micrometer.tracing.Tracer
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
class OutboxFactoryTest {
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    private val tracer: Tracer = mock()
    private val outboxFactory = OutboxFactory(objectMapper, tracer)

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
        given(tracer.currentSpan()).willReturn(null)

        // when
        val outbox = outboxFactory.from(event)

        // then
        outbox.eventType shouldBe OutboxEventType.PAYMENT_AUTHORIZED
        outbox.status shouldBe OutboxStatus.PENDING
        outbox.eventId shouldBe event.eventId
        outbox.occurredAt shouldBe event.occurredAt

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
        given(tracer.currentSpan()).willReturn(null)

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

    @Test
    fun `활성 span이 있으면 traceparent가 W3C 포맷으로 설정되어야 한다`() {
        // given
        val event =
            PaymentAuthorizedEvent(
                eventId = UUID.randomUUID(),
                orderId = 300L,
                userId = 3L,
                amount = BigDecimal("5000"),
                pgTransactionId = "PG-TX-002",
                occurredAt = LocalDateTime.now(),
            )
        val traceId = "4bf92f3577b34da6a3ce929d0e0e4736"
        val spanId = "00f067aa0ba902b7"
        val traceContext: TraceContext = mock()
        val span: Span = mock()
        given(traceContext.traceId()).willReturn(traceId)
        given(traceContext.spanId()).willReturn(spanId)
        given(span.context()).willReturn(traceContext)
        given(tracer.currentSpan()).willReturn(span)

        // when
        val outbox = outboxFactory.from(event)

        // then
        outbox.traceparent shouldNotBe null
        outbox.traceparent shouldBe "00-$traceId-$spanId-01"
        Regex("^00-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$").matches(outbox.traceparent!!) shouldBe true
    }

    @Test
    fun `활성 span이 없으면 traceparent가 null이어야 한다`() {
        // given
        val event =
            PaymentFailedEvent(
                eventId = UUID.randomUUID(),
                orderId = 400L,
                userId = 4L,
                failReason = "잔액 부족",
                occurredAt = LocalDateTime.now(),
            )
        given(tracer.currentSpan()).willReturn(null)

        // when
        val outbox = outboxFactory.from(event)

        // then
        outbox.traceparent shouldBe null
    }
}
