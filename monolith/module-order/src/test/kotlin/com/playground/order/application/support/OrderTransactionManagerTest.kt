package com.playground.order.application.support

import com.playground.order.application.port.outbound.OrderCommandPort
import com.playground.order.application.port.outbound.OrderEventPort
import com.playground.order.application.port.outbound.OutboxCommandPort
import com.playground.order.application.provider.OrderExternalDataProvider
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.domain.outbox.OrderEventOutbox
import com.playground.order.domain.outbox.OutboxEventType
import com.playground.order.domain.outbox.OutboxStatus
import com.playground.order.fixture.application.domain.AddressInfoTestFixture
import com.playground.order.fixture.application.domain.OrderDomainTestFixture
import com.playground.order.fixture.application.domain.ProductInfoTestFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
class OrderTransactionManagerTest {

    private val orderCommandPort: OrderCommandPort = mock()
    private val outboxCommandPort: OutboxCommandPort = mock()
    private val orderEventPort: OrderEventPort = mock()
    private val outboxFactory: OutboxFactory = mock()
    private val orderExternalDataProvider: OrderExternalDataProvider = mock()

    private val orderTransactionManager = OrderTransactionManager(
        orderCommandPort = orderCommandPort,
        outboxCommandPort = outboxCommandPort,
        orderEventPort = orderEventPort,
        outboxFactory = outboxFactory,
        orderExternalDataProvider = orderExternalDataProvider,
    )

    @Test
    fun `getExternalData 호출 시 ExternalDataProvider 두 메서드를 각각 호출하고 Pair로 반환해야 한다`() {
        // given
        val productIds = listOf(1L, 2L)
        val userId = 2L
        val addressId = 10L
        val mockProductInfoMap = ProductInfoTestFixture.mockProductInfos()
        val mockAddressInfo = AddressInfoTestFixture.mockAddressInfo()

        given(orderExternalDataProvider.getVerifiedProductInfos(productIds))
            .willReturn(mockProductInfoMap)
        given(orderExternalDataProvider.getAddressInfoByAddressId(userId, addressId))
            .willReturn(mockAddressInfo)

        // when
        val result = orderTransactionManager.getExternalData(productIds, userId, addressId)

        // then
        verify(orderExternalDataProvider).getVerifiedProductInfos(productIds)
        verify(orderExternalDataProvider).getAddressInfoByAddressId(userId, addressId)

        result.first shouldBe mockProductInfoMap
        result.second shouldBe mockAddressInfo
    }

    @Test
    fun `saveOrderWithEventAndOutbox 호출 시 주문 저장, 이벤트 발행, Outbox 저장이 순서대로 수행되어야 한다`() {
        // given
        val orderId = 1L
        val userId = 2L
        val mockOrder = OrderDomainTestFixture.mockOrder(id = orderId, userId = userId)
        val savedOrder = OrderDomainTestFixture.mockOrder(id = orderId, userId = userId)
        val mockOutbox = OrderEventOutbox(
            id = 100L,
            eventId = UUID.randomUUID(),
            orderId = orderId,
            eventType = OutboxEventType.ORDER_CREATED,
            payload = "{}",
            status = OutboxStatus.PENDING,
            occurredAt = LocalDateTime.now(),
        )

        given(orderCommandPort.save(any())).willReturn(savedOrder)
        given(outboxFactory.from(any<OrderCreatedEvent>())).willReturn(mockOutbox)
        given(outboxCommandPort.save(any())).willReturn(mockOutbox)

        // when
        val result = orderTransactionManager.saveOrderWithEventAndOutbox(mockOrder)

        // then
        verify(orderCommandPort).save(mockOrder)

        val eventCaptor = argumentCaptor<OrderCreatedEvent>()
        verify(orderEventPort).publish(eventCaptor.capture())
        with(eventCaptor.firstValue) {
            orderId shouldBe orderId
            userId shouldBe userId
        }

        verify(outboxFactory).from(eventCaptor.firstValue)
        verify(outboxCommandPort).save(mockOutbox)

        result shouldBe savedOrder
    }

    @Test
    fun `saveOrderWithEventAndOutbox 호출 시 orderCommandPort 저장 실패하면 이후 단계가 호출되지 않아야 한다`() {
        // given
        val mockOrder = OrderDomainTestFixture.mockOrder()

        given(orderCommandPort.save(any())).willThrow(RuntimeException("DB 저장 실패"))

        // when & then
        shouldThrow<RuntimeException> {
            orderTransactionManager.saveOrderWithEventAndOutbox(mockOrder)
        }

        verify(orderEventPort, never()).publish(any())
        verify(outboxCommandPort, never()).save(any())
    }
}
