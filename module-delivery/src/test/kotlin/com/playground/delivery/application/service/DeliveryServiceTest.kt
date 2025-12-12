package com.playground.delivery.application.service

import com.playground.delivery.application.port.outbound.DeliveryCommandPort
import com.playground.delivery.application.port.outbound.DeliveryQueryPort
import com.playground.delivery.fixture.application.command.DeliveryCommandTestFixture
import com.playground.delivery.fixture.application.domain.DeliveryDomainTestFixture
import com.playground.delivery.domain.model.Delivery
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.Optional
import io.kotest.matchers.shouldBe

@Suppress("NonAsciiCharacters")
class DeliveryServiceTest {

    private val deliveryCommandPort: DeliveryCommandPort = mock()
    private val deliveryQueryPort: DeliveryQueryPort = mock()
    private val deliveryService = DeliveryService(
        deliveryCommandPort = deliveryCommandPort,
        deliveryQueryPort = deliveryQueryPort,
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

        result shouldBe savedDelivery
    }
}
