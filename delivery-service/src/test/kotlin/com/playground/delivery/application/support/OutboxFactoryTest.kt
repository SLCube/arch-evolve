package com.playground.delivery.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.playground.delivery.domain.outbox.OutboxEventType
import com.playground.delivery.domain.outbox.OutboxStatus
import com.playground.delivery.fixture.application.domain.DeliveryDomainTestFixture
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micrometer.tracing.Span
import io.micrometer.tracing.TraceContext
import io.micrometer.tracing.Tracer
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class OutboxFactoryTest {
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    private val tracer: Tracer = mock()
    private val outboxFactory = OutboxFactory(objectMapper, tracer)

    @Test
    fun `deliveryCreated 호출 시 DELIVERY_CREATED 타입 PENDING Outbox가 생성되어야 한다`() {
        // given
        val delivery =
            DeliveryDomainTestFixture.mockDelivery(
                id = 1L,
                orderId = 100L,
                userId = 2L,
            )
        whenever(tracer.currentSpan()).thenReturn(null)

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
        outbox.traceparent shouldBe null
    }

    @Test
    fun `현재 span이 존재하면 traceparent가 W3C 포맷으로 설정되어야 한다`() {
        // given
        val delivery = DeliveryDomainTestFixture.mockDelivery()
        val traceContext: TraceContext = mock()
        val span: Span = mock()
        whenever(tracer.currentSpan()).thenReturn(span)
        whenever(span.context()).thenReturn(traceContext)
        val traceId = "4BF92F3577B34DA6A3CE929D0E0E4736"
        val spanId = "00F067AA0BA902B7"
        whenever(traceContext.traceId()).thenReturn(traceId)
        whenever(traceContext.spanId()).thenReturn(spanId)

        // when
        val outbox = outboxFactory.deliveryCreated(delivery)

        // then
        outbox.traceparent shouldBe "00-${traceId.lowercase()}-${spanId.lowercase()}-01"
        Regex("^00-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$").matches(outbox.traceparent!!) shouldBe true
    }

    @Test
    fun `현재 span이 없으면 traceparent는 null이어야 한다`() {
        // given
        val delivery = DeliveryDomainTestFixture.mockDelivery()
        whenever(tracer.currentSpan()).thenReturn(null)

        // when
        val outbox = outboxFactory.deliveryCreated(delivery)

        // then
        outbox.traceparent shouldBe null
    }

    @Test
    fun `span context가 null이면 traceparent는 null이어야 한다`() {
        // given
        val delivery = DeliveryDomainTestFixture.mockDelivery()
        val span: Span = mock()
        whenever(tracer.currentSpan()).thenReturn(span)
        whenever(span.context()).thenReturn(null)

        // when
        val outbox = outboxFactory.deliveryCreated(delivery)

        // then
        outbox.traceparent shouldBe null
        outbox.eventType shouldNotBe null
    }
}
