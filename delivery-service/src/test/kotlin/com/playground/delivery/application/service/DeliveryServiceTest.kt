package com.playground.delivery.application.service

import com.playground.delivery.application.port.outbound.DeliveryCommandPort
import com.playground.delivery.application.port.outbound.DeliveryQueryPort
import com.playground.delivery.application.port.outbound.OutboxCommandPort
import com.playground.delivery.application.support.OutboxFactory
import com.playground.delivery.fixture.application.command.DeliveryCommandTestFixture
import com.playground.delivery.fixture.application.domain.DeliveryDomainTestFixture
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.Optional

@Suppress("NonAsciiCharacters")
class DeliveryServiceTest {
    private val deliveryCommandPort: DeliveryCommandPort = mock()
    private val deliveryQueryPort: DeliveryQueryPort = mock()
    private val outboxCommandPort: OutboxCommandPort = mock()
    private val outboxFactory: OutboxFactory = mock()

    private val deliveryService =
        DeliveryService(
            deliveryCommandPort = deliveryCommandPort,
            deliveryQueryPort = deliveryQueryPort,
            outboxCommandPort = outboxCommandPort,
            outboxFactory = outboxFactory,
        )

    @Test
    fun `동일한 orderId로 배송이 이미 존재하면 save 없이 기존 배송을 반환해야 한다`() {
        // given
        val existing =
            DeliveryDomainTestFixture.mockDelivery(
                orderId = 100L,
                userId = 2L,
            )
        val command =
            DeliveryCommandTestFixture.createCommand(
                orderId = existing.orderId,
                userId = existing.userId,
            )

        given(deliveryQueryPort.findByOrderId(command.orderId))
            .willReturn(Optional.of(existing))

        // when
        val result = deliveryService.createDelivery(command)

        // then
        result shouldBe existing

        verify(deliveryQueryPort).findByOrderId(command.orderId)
        verify(deliveryCommandPort, never()).save(any())
        verify(outboxCommandPort, never()).save(any())
    }

    @Test
    fun `신규 배송 생성 시 save 후 outbox를 저장하고 저장된 배송을 반환해야 한다`() {
        // given
        val command =
            DeliveryCommandTestFixture.createCommand(
                orderId = 200L,
                userId = 5L,
            )
        val savedDelivery =
            DeliveryDomainTestFixture.mockDelivery(
                id = 1L,
                orderId = command.orderId,
                userId = command.userId,
            )

        given(deliveryQueryPort.findByOrderId(command.orderId))
            .willReturn(Optional.empty())
        given(deliveryCommandPort.save(any()))
            .willReturn(savedDelivery)
        given(outboxFactory.deliveryCreated(any()))
            .willReturn(mock())

        // when
        val result = deliveryService.createDelivery(command)

        // then
        result.orderId shouldBe command.orderId
        result.userId shouldBe command.userId
        result.id shouldNotBe null

        verify(deliveryCommandPort).save(any())
        verify(outboxFactory).deliveryCreated(savedDelivery)
        verify(outboxCommandPort).save(any())
    }

    @Test
    fun `save 후 outbox에 저장되는 orderId가 저장된 배송의 orderId와 일치해야 한다`() {
        // given
        val command =
            DeliveryCommandTestFixture.createCommand(
                orderId = 300L,
                userId = 7L,
            )
        val savedDelivery =
            DeliveryDomainTestFixture.mockDelivery(
                id = 10L,
                orderId = command.orderId,
                userId = command.userId,
            )

        given(deliveryQueryPort.findByOrderId(command.orderId))
            .willReturn(Optional.empty())
        given(deliveryCommandPort.save(any()))
            .willReturn(savedDelivery)
        given(outboxFactory.deliveryCreated(any()))
            .willReturn(mock())

        // when
        deliveryService.createDelivery(command)

        // then
        val deliveryCaptor = argumentCaptor<com.playground.delivery.domain.model.Delivery>()
        verify(outboxFactory).deliveryCreated(deliveryCaptor.capture())
        deliveryCaptor.firstValue.orderId shouldBe savedDelivery.orderId
        deliveryCaptor.firstValue.id shouldBe savedDelivery.id
    }
}
