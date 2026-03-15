package com.playground.order.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.playground.order.contract.domain.event.OrderCompletedEvent
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.domain.outbox.OutboxEventType
import com.playground.order.domain.outbox.OutboxStatus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micrometer.tracing.Span
import io.micrometer.tracing.TraceContext
import io.micrometer.tracing.Tracer
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
class OutboxFactoryTest {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
    private val tracer: Tracer = mock()
    private val outboxFactory = OutboxFactory(objectMapper, tracer)

    @Test
    fun `활성 span이 있을 때 OrderCreatedEvent로부터 traceparent가 포함된 Outbox를 생성해야 한다`() {
        // given
        val traceId = "4bf92f3577b34da6a3ce929d0e0e4736"
        val spanId = "00f067aa0ba902b7"
        val mockContext: TraceContext = mock()
        val mockSpan: Span = mock()

        given(mockContext.traceId()).willReturn(traceId)
        given(mockContext.spanId()).willReturn(spanId)
        given(mockSpan.context()).willReturn(mockContext)
        given(tracer.currentSpan()).willReturn(mockSpan)

        val event = OrderCreatedEvent(
            eventId = UUID.randomUUID(),
            orderId = 1L,
            userId = 2L,
            products = emptyList(),
            totalAmount = 10000.toBigDecimal(),
            occurredAt = LocalDateTime.now(),
        )

        // when
        val outbox = outboxFactory.from(event)

        // then
        outbox.eventType shouldBe OutboxEventType.ORDER_CREATED
        outbox.status shouldBe OutboxStatus.PENDING
        outbox.orderId shouldBe event.orderId
        outbox.eventId shouldBe event.eventId
        outbox.traceparent shouldBe "00-$traceId-$spanId-01"
    }

    @Test
    fun `활성 span이 없을 때 OrderCreatedEvent로부터 traceparent가 null인 Outbox를 생성해야 한다`() {
        // given
        given(tracer.currentSpan()).willReturn(null)

        val event = OrderCreatedEvent(
            eventId = UUID.randomUUID(),
            orderId = 1L,
            userId = 2L,
            products = emptyList(),
            totalAmount = 10000.toBigDecimal(),
            occurredAt = LocalDateTime.now(),
        )

        // when
        val outbox = outboxFactory.from(event)

        // then
        outbox.traceparent shouldBe null
    }

    @Test
    fun `활성 span이 있을 때 OrderCompletedEvent로부터 traceparent가 포함된 Outbox를 생성해야 한다`() {
        // given
        val traceId = "4bf92f3577b34da6a3ce929d0e0e4736"
        val spanId = "00f067aa0ba902b7"
        val mockContext: TraceContext = mock()
        val mockSpan: Span = mock()

        given(mockContext.traceId()).willReturn(traceId)
        given(mockContext.spanId()).willReturn(spanId)
        given(mockSpan.context()).willReturn(mockContext)
        given(tracer.currentSpan()).willReturn(mockSpan)

        val event = OrderCompletedEvent(
            eventId = UUID.randomUUID(),
            orderId = 2L,
            userId = 3L,
            products = emptyList(),
            totalAmount = 15000.toBigDecimal(),
            receiverName = "홍길동",
            receiverPhoneNumber = "010-1234-5678",
            zipCode = "12345",
            baseAddress = "서울특별시 강남구",
            detailAddress = "역삼동 123-45",
            occurredAt = LocalDateTime.now(),
        )

        // when
        val outbox = outboxFactory.from(event)

        // then
        outbox.eventType shouldBe OutboxEventType.ORDER_COMPLETED
        outbox.status shouldBe OutboxStatus.PENDING
        outbox.orderId shouldBe event.orderId
        outbox.traceparent shouldNotBe null
        outbox.traceparent shouldBe "00-$traceId-$spanId-01"
    }

    @Test
    fun `활성 span이 없을 때 OrderCompletedEvent로부터 traceparent가 null인 Outbox를 생성해야 한다`() {
        // given
        given(tracer.currentSpan()).willReturn(null)

        val event = OrderCompletedEvent(
            eventId = UUID.randomUUID(),
            orderId = 2L,
            userId = 3L,
            products = emptyList(),
            totalAmount = 15000.toBigDecimal(),
            receiverName = "홍길동",
            receiverPhoneNumber = "010-1234-5678",
            zipCode = "12345",
            baseAddress = "서울특별시 강남구",
            detailAddress = "역삼동 123-45",
            occurredAt = LocalDateTime.now(),
        )

        // when
        val outbox = outboxFactory.from(event)

        // then
        outbox.traceparent shouldBe null
    }
}
