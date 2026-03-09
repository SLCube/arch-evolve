package com.playground.payment.application.service

import com.playground.payment.application.port.outbound.OutboxCommandPort
import com.playground.payment.application.port.outbound.OutboxQueryPort
import com.playground.payment.application.port.outbound.PaymentEventPublisherPort
import com.playground.payment.domain.outbox.OutboxEventType
import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.domain.outbox.PaymentEventOutbox
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
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
    fun `PENDING 이벤트를 조회하여 Kafka로 발행하고 PUBLISHED로 벌크 업데이트해야 한다`() {
        // given
        val outbox = createOutbox(OutboxEventType.PAYMENT_AUTHORIZED)
        val outboxes = listOf(outbox)
        given(outboxQueryPort.findByStatus(any(), any())).willReturn(outboxes)
        given(paymentEventPublisherPort.publishAll(outboxes)).willReturn(outboxes)

        // when
        paymentOutboxEventService.pollAndPublishEvents()

        // then
        verify(outboxQueryPort).findByStatus(any(), any())
        verify(paymentEventPublisherPort).publishAll(outboxes)
        verify(outboxCommandPort).bulkMarkAsPublished(listOf(1L))
    }

    @Test
    fun `PENDING 이벤트가 없으면 발행하지 않아야 한다`() {
        // given
        given(outboxQueryPort.findByStatus(any(), any())).willReturn(emptyList())

        // when
        paymentOutboxEventService.pollAndPublishEvents()

        // then
        verify(paymentEventPublisherPort, never()).publishAll(any())
        verify(outboxCommandPort, never()).bulkMarkAsPublished(any())
    }

    @Test
    fun `Kafka 발행이 모두 실패하면 벌크 업데이트하지 않아야 한다`() {
        // given
        val outbox = createOutbox(OutboxEventType.PAYMENT_AUTHORIZED)
        val outboxes = listOf(outbox)
        given(outboxQueryPort.findByStatus(any(), any())).willReturn(outboxes)
        given(paymentEventPublisherPort.publishAll(outboxes)).willReturn(emptyList())

        // when
        paymentOutboxEventService.pollAndPublishEvents()

        // then
        verify(paymentEventPublisherPort).publishAll(outboxes)
        verify(outboxCommandPort, never()).bulkMarkAsPublished(any())
    }

    private fun createOutbox(eventType: OutboxEventType): PaymentEventOutbox =
        PaymentEventOutbox(
            id = 1L,
            eventId = UUID.randomUUID(),
            orderId = 1L,
            eventType = eventType,
            payload = "{}",
            status = OutboxStatus.PENDING,
            occurredAt = LocalDateTime.now(),
        )
}
