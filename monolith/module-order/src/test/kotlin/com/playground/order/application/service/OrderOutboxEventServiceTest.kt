package com.playground.order.application.service

import com.playground.order.application.port.outbound.OrderEventPublisherPort
import com.playground.order.application.port.outbound.OutboxCommandPort
import com.playground.order.application.port.outbound.OutboxMetricsPort
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
    private val outboxMetricsPort: OutboxMetricsPort = mock()

    private val orderOutboxEventService =
        OrderOutboxEventService(
            outboxQueryPort = outboxQueryPort,
            outboxCommandPort = outboxCommandPort,
            orderEventPublisherPort = orderEventPublisherPort,
            outboxMetricsPort = outboxMetricsPort,
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

    @Test
    fun `발행 실패한 이벤트의 retryCount가 MAX_RETRY 미만이면 retryCount를 증가시켜야 한다`() {
        // given
        val outbox = createOutbox(OutboxEventType.ORDER_CREATED, retryCount = 0)
        val outboxes = listOf(outbox)
        given(outboxQueryPort.findByStatus(any(), any())).willReturn(outboxes)
        given(orderEventPublisherPort.publishAll(outboxes)).willReturn(emptyList())

        // when
        orderOutboxEventService.pollAndPublishEvents()

        // then
        verify(outboxCommandPort).bulkIncrementRetryCount(listOf(1L))
        verify(outboxCommandPort, never()).bulkMarkAsFailed(any())
    }

    @Test
    fun `발행 실패한 이벤트의 retryCount가 MAX_RETRY에 도달하면 FAILED로 전환해야 한다`() {
        // given
        val outbox = createOutbox(OutboxEventType.ORDER_CREATED, retryCount = 2)
        val outboxes = listOf(outbox)
        given(outboxQueryPort.findByStatus(any(), any())).willReturn(outboxes)
        given(orderEventPublisherPort.publishAll(outboxes)).willReturn(emptyList())

        // when
        orderOutboxEventService.pollAndPublishEvents()

        // then
        verify(outboxCommandPort).bulkMarkAsFailed(listOf(1L))
        verify(outboxCommandPort, never()).bulkIncrementRetryCount(any())
    }

    @Test
    fun `발행 실패 목록이 없으면 retryCount 증가와 FAILED 전환을 하지 않아야 한다`() {
        // given
        val outbox = createOutbox(OutboxEventType.ORDER_CREATED)
        val outboxes = listOf(outbox)
        given(outboxQueryPort.findByStatus(any(), any())).willReturn(outboxes)
        given(orderEventPublisherPort.publishAll(outboxes)).willReturn(outboxes)

        // when
        orderOutboxEventService.pollAndPublishEvents()

        // then
        verify(outboxCommandPort, never()).bulkIncrementRetryCount(any())
        verify(outboxCommandPort, never()).bulkMarkAsFailed(any())
    }

    @Test
    fun `recordMetrics는 PENDING과 FAILED 건수를 메트릭 포트에 기록해야 한다`() {
        // given
        given(outboxQueryPort.countByStatus(OutboxStatus.PENDING)).willReturn(5L)
        given(outboxQueryPort.countByStatus(OutboxStatus.FAILED)).willReturn(2L)

        // when
        orderOutboxEventService.recordMetrics()

        // then
        verify(outboxMetricsPort).recordPendingCount(5L)
        verify(outboxMetricsPort).recordFailedCount(2L)
    }

    private fun createOutbox(
        eventType: OutboxEventType,
        retryCount: Int = 0,
    ): OrderEventOutbox =
        OrderEventOutbox(
            id = 1L,
            eventId = UUID.randomUUID(),
            orderId = 1L,
            eventType = eventType,
            payload = "{}",
            status = OutboxStatus.PENDING,
            occurredAt = LocalDateTime.now(),
            retryCount = retryCount,
        )
}
