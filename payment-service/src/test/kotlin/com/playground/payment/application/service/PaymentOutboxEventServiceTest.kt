package com.playground.payment.application.service

import com.playground.payment.application.port.outbound.OutboxCommandPort
import com.playground.payment.application.port.outbound.OutboxQueryPort
import com.playground.payment.application.port.outbound.PaymentEventPublisherPort
import com.playground.payment.domain.outbox.OutboxEventType
import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.domain.outbox.PaymentEventOutbox
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
class PaymentOutboxEventServiceTest {
    private val outboxQueryPort: OutboxQueryPort = mock()
    private val outboxCommandPort: OutboxCommandPort = mock()
    private val paymentEventPublisherPort: PaymentEventPublisherPort = mock()

    private val paymentOutboxEventService =
        PaymentOutboxEventService(
            outboxQueryPort = outboxQueryPort,
            outboxCommandPort = outboxCommandPort,
            paymentEventPublisherPort = paymentEventPublisherPort,
        )

    @Test
    fun `PENDING 상태 이벤트 목록을 조회해야 한다`() {
        // given
        val outbox = createOutbox(OutboxEventType.PAYMENT_AUTHORIZED)
        given(outboxQueryPort.findByStatus(any(), any())).willReturn(listOf(outbox))

        // when
        val result = paymentOutboxEventService.findPendingEvents()

        // then
        result.size shouldBe 1
        result[0] shouldBe outbox
        verify(outboxQueryPort).findByStatus(any(), any())
    }

    @Test
    fun `이벤트를 Kafka로 발행하고 PUBLISHED로 상태를 변경해야 한다`() {
        // given
        val outbox = createOutbox(OutboxEventType.PAYMENT_AUTHORIZED)
        given(outboxCommandPort.save(any())).willReturn(outbox)

        // when
        paymentOutboxEventService.publishEvent(outbox)

        // then
        verify(paymentEventPublisherPort).publish(outbox)
        verify(outboxCommandPort).save(outbox)
        outbox.status shouldBe OutboxStatus.PUBLISHED
    }

    private fun createOutbox(eventType: OutboxEventType): PaymentEventOutbox =
        PaymentEventOutbox(
            id = 1L,
            eventId = UUID.randomUUID(),
            eventType = eventType,
            payload = "{}",
            status = OutboxStatus.PENDING,
            occurredAt = LocalDateTime.now(),
            requestId = "test-request-id",
        )
}
