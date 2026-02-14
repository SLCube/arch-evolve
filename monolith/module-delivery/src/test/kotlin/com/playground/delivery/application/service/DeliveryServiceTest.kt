package com.playground.delivery.application.service

import com.playground.delivery.application.port.outbound.DeliveryCommandPort
import com.playground.delivery.application.port.outbound.DeliveryEventPort
import com.playground.delivery.application.port.outbound.DeliveryQueryPort
import com.playground.delivery.contract.domain.event.DeliveryCompletedEvent
import com.playground.delivery.contract.domain.event.DeliveryCreatedEvent
import com.playground.delivery.contract.domain.event.DeliveryStartedEvent
import com.playground.delivery.fixture.application.command.DeliveryCommandTestFixture
import com.playground.delivery.fixture.application.domain.DeliveryDomainTestFixture
import com.playground.delivery.domain.enum.DeliveryStatus
import com.playground.delivery.domain.exception.DeliveryNotFoundException
import com.playground.delivery.domain.model.Delivery
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.Optional
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@Suppress("NonAsciiCharacters")
class DeliveryServiceTest {

    private val deliveryCommandPort: DeliveryCommandPort = mock()
    private val deliveryQueryPort: DeliveryQueryPort = mock()
    private val deliveryEventPort: DeliveryEventPort = mock()
    private val deliveryService = DeliveryService(
        deliveryCommandPort = deliveryCommandPort,
        deliveryQueryPort = deliveryQueryPort,
        deliveryEventPort = deliveryEventPort,
    )

    @Test
    fun `주문에 대한 배송이 이미 존재하면 기존 배송을 반환해야 한다`() {
        // given
        val command = DeliveryCommandTestFixture.createDeliveryCommand(orderId = 1L)
        val existingDelivery = DeliveryDomainTestFixture.createDelivery(orderId = command.orderId)

        given(deliveryQueryPort.findByOrderId(command.orderId))
            .willReturn(Optional.of(existingDelivery))

        // when
        val result = deliveryService.createDelivery(command)

        // then
        result shouldBe existingDelivery
        verify(deliveryQueryPort).findByOrderId(command.orderId)
        verify(deliveryCommandPort, never()).save(any())
        verify(deliveryEventPort, never()).publish(any())
    }

    @Test
    fun `주문에 대한 배송이 없다면 새 배송을 생성하고 저장해야 한다`() {
        // given
        val command = DeliveryCommandTestFixture.createDeliveryCommand(orderId = 2L)
        val savedDelivery = DeliveryDomainTestFixture.createDelivery(
            id = 10L,
            orderId = command.orderId,
            userId = command.userId,
            receiverName = command.receiverName,
            receiverPhoneNumber = command.receiverPhoneNumber,
            zipCode = command.zipCode,
            baseAddress = command.baseAddress,
            detailAddress = command.detailAddress,
        )

        given(deliveryQueryPort.findByOrderId(command.orderId))
            .willReturn(Optional.empty())

        given(deliveryCommandPort.save(any<Delivery>()))
            .willReturn(savedDelivery)

        // when
        val result = deliveryService.createDelivery(command)

        // then
        verify(deliveryQueryPort).findByOrderId(command.orderId)
        verify(deliveryCommandPort).save(check {
            it.orderId shouldBe command.orderId
            it.userId shouldBe command.userId
            it.deliveryReceiver.receiverName shouldBe command.receiverName
            it.deliveryReceiver.receiverPhoneNumber shouldBe command.receiverPhoneNumber
            it.deliveryAddress.zipCode shouldBe command.zipCode
        })
        verify(deliveryEventPort).publish(check<DeliveryCreatedEvent> {
            it.orderId shouldBe command.orderId
            it.userId shouldBe command.userId
        })

        result shouldBe savedDelivery
    }

    @Test
    fun `배송 시작 요청 시 배송 상태가 SHIPPING으로 변경되어 저장되어야 한다`() {
        // given
        val orderId = 10L
        val existingDelivery = DeliveryDomainTestFixture.createDelivery(id = 1L, orderId = orderId, status = DeliveryStatus.PENDING)
        val savedDelivery = DeliveryDomainTestFixture.createDelivery(id = 1L, orderId = orderId, status = DeliveryStatus.SHIPPING)

        given(deliveryQueryPort.findByOrderIdOrThrow(orderId))
            .willReturn(existingDelivery)

        given(deliveryCommandPort.update(any<Delivery>()))
            .willReturn(savedDelivery)

        // when
        val result = deliveryService.startDelivery(orderId)

        // then
        verify(deliveryCommandPort).update(check {
            it.orderId shouldBe orderId
            it.deliveryStatus shouldBe DeliveryStatus.SHIPPING
            it.shippedAt shouldNotBe null
            it.updatedAt shouldNotBe null
        })
        verify(deliveryEventPort).publish(check<DeliveryStartedEvent> {
            it.orderId shouldBe orderId
            it.userId shouldBe existingDelivery.userId
            it.shippedAt shouldNotBe null
        })
        result.deliveryStatus shouldBe DeliveryStatus.SHIPPING
    }

    @Test
    fun `배송 완료 요청 시 배송 상태가 DELIVERED로 변경되어 저장되어야 한다`() {
        // given
        val orderId = 11L
        val existingDelivery = DeliveryDomainTestFixture.createDelivery(id = 2L, orderId = orderId, status = DeliveryStatus.SHIPPING)
        val savedDelivery = DeliveryDomainTestFixture.createDelivery(id = 2L, orderId = orderId, status = DeliveryStatus.DELIVERED)

        given(deliveryQueryPort.findByOrderIdOrThrow(orderId))
            .willReturn(existingDelivery)

        given(deliveryCommandPort.update(any<Delivery>()))
            .willReturn(savedDelivery)

        // when
        val result = deliveryService.completeDelivery(orderId)

        // then
        verify(deliveryCommandPort).update(check {
            it.orderId shouldBe orderId
            it.deliveryStatus shouldBe DeliveryStatus.DELIVERED
            it.deliveredAt shouldNotBe null
            it.updatedAt shouldNotBe null
        })
        verify(deliveryEventPort).publish(check<DeliveryCompletedEvent> {
            it.orderId shouldBe orderId
            it.userId shouldBe existingDelivery.userId
            it.deliveredAt shouldNotBe null
        })
        result.deliveryStatus shouldBe DeliveryStatus.DELIVERED
    }

    @Test
    fun `배송 시작 요청 시 주문에 대한 배송이 없으면 예외를 던져야 한다`() {
        // given
        val orderId = 999L
        given(deliveryQueryPort.findByOrderIdOrThrow(orderId))
            .willThrow(DeliveryNotFoundException(orderId))

        // when & then
        shouldThrow<DeliveryNotFoundException> {
            deliveryService.startDelivery(orderId)
        }
        verify(deliveryEventPort, never()).publish(any())
    }
}
