package com.playground.order.application.service

import com.playground.order.application.port.outbound.OrderEventPublisherPort
import com.playground.order.application.port.outbound.OutboxCommandPort
import com.playground.order.application.port.outbound.OutboxQueryPort
import com.playground.order.domain.outbox.OrderEventOutbox
import com.playground.order.domain.outbox.OutboxEventType
import com.playground.order.domain.outbox.OutboxStatus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
class OrderOutboxEventServiceTest {
    private val outboxQueryPort: OutboxQueryPort = mock()
    private val outboxCommandPort: OutboxCommandPort = mock()
    private val orderEventPublisherPort: OrderEventPublisherPort = mock()

    private val orderOutboxEventService =
        OrderOutboxEventService(
            outboxQueryPort = outboxQueryPort,
            outboxCommandPort = outboxCommandPort,
            orderEventPublisherPort = orderEventPublisherPort,
        )

    @Test
    fun `PENDING 이벤트를 조회하여 Kafka로 발행하고 PUBLISHED로 벌크 업데이트해야 한다`() {
        // given
        val outbox = createOutbox(OutboxEventType.ORDER_CREATED)
        val outboxes = listOf(outbox)
        given(outboxQueryPort.findByStatus(any(), any())).willReturn(outboxes)
        given(orderEventPublisherPort.publishAll(outboxes)).willReturn(outboxes)

        // when
        orderOutboxEventService.pollAndPublishEvents()

        // then
        verify(outboxQueryPort).findByStatus(any(), any())
        verify(orderEventPublisherPort).publishAll(outboxes)
        verify(outboxCommandPort).bulkMarkAsPublished(listOf(1L))
    }

    @Test
    fun `PENDING 이벤트가 없으면 발행하지 않아야 한다`() {
        // given
        given(outboxQueryPort.findByStatus(any(), any())).willReturn(emptyList())

        // when
        orderOutboxEventService.pollAndPublishEvents()

        // then
        verify(orderEventPublisherPort, never()).publishAll(any())
        verify(outboxCommandPort, never()).bulkMarkAsPublished(any())
    }

    @Test
    fun `Kafka 발행이 모두 실패하면 벌크 업데이트하지 않아야 한다`() {
        // given
        val outbox = createOutbox(OutboxEventType.ORDER_CREATED)
        val outboxes = listOf(outbox)
        given(outboxQueryPort.findByStatus(any(), any())).willReturn(outboxes)
        given(orderEventPublisherPort.publishAll(outboxes)).willReturn(emptyList())

        // when
        orderOutboxEventService.pollAndPublishEvents()

        // then
        verify(orderEventPublisherPort).publishAll(outboxes)
        verify(outboxCommandPort, never()).bulkMarkAsPublished(any())
    }

    private fun createOutbox(eventType: OutboxEventType): OrderEventOutbox =
        OrderEventOutbox(
            id = 1L,
            eventId = UUID.randomUUID(),
            orderId = 1L,
            eventType = eventType,
            payload = "{}",
            status = OutboxStatus.PENDING,
            occurredAt = LocalDateTime.now(),
        )
}
