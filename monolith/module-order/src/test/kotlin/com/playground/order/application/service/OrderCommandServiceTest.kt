package com.playground.order.application.service

import com.playground.order.application.port.inbound.command.OrderCancelCommand
import com.playground.order.application.port.inbound.command.OrderCompleteCommand
import com.playground.order.application.port.inbound.command.OrderFailCommand
import com.playground.order.application.port.outbound.OrderCommandPort
import com.playground.order.application.port.outbound.OrderEventPort
import com.playground.order.application.port.outbound.OrderQueryPort
import com.playground.order.application.port.outbound.OutboxCommandPort
import com.playground.order.application.support.OrderTransactionManager
import com.playground.order.application.support.OutboxFactory
import com.playground.order.contract.domain.event.OrderCompletedEvent
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.contract.domain.event.OrderFailedEvent
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.exception.OrderAccessDeniedException
import com.playground.order.domain.exception.OrderStatusInvalidException
import com.playground.order.domain.model.Order
import com.playground.order.domain.outbox.OrderEventOutbox
import com.playground.order.fixture.application.command.OrderCommandTestFixture
import com.playground.order.fixture.application.domain.AddressInfoTestFixture
import com.playground.order.fixture.application.domain.OrderDomainTestFixture
import com.playground.order.fixture.application.domain.ProductInfoTestFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@Suppress("NonAsciiCharacters")
class OrderCommandServiceTest {

    private val orderTransactionManager: OrderTransactionManager = mock()
    private val orderQueryPort: OrderQueryPort = mock()
    private val orderCommandPort: OrderCommandPort = mock()
    private val orderEventPort: OrderEventPort = mock()
    private val outboxCommandPort: OutboxCommandPort = mock()
    private val outboxFactory: OutboxFactory = mock()

    private val orderCommandService: OrderCommandService = OrderCommandService(
        orderTransactionManager = orderTransactionManager,
        orderQueryPort = orderQueryPort,
        orderCommandPort = orderCommandPort,
        orderEventPort = orderEventPort,
        outboxCommandPort = outboxCommandPort,
        outboxFactory = outboxFactory,
    )

    @Test
    fun `주문 생성 시 PENDING 상태로 저장되고 Spring Event와 Outbox가 생성되어야 한다`() {
        // given
        val userId = 2L
        val orderId = 1L
        val command = OrderCommandTestFixture.createOrderCommand(userId = userId)
        val mockProductInfo = ProductInfoTestFixture.mockProductInfos()
        val mockAddressInfo = AddressInfoTestFixture.mockAddressInfo()
        val mockOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            userId = userId,
        )

        given(orderTransactionManager.getExternalData(any(), any(), any()))
            .willReturn(mockProductInfo to mockAddressInfo)
        given(orderTransactionManager.saveOrderWithEventAndOutbox(any<Order>()))
            .willReturn(mockOrder)

        // when
        val createdOrder = orderCommandService.createOrder(command)

        // then
        verify(orderTransactionManager).getExternalData(any(), any(), any())
        verify(orderTransactionManager).saveOrderWithEventAndOutbox(any<Order>())

        createdOrder.id shouldBe orderId
        createdOrder.status shouldBe OrderStatus.PENDING
    }

    @Test
    fun `결제 완료 Command 수신 시 PENDING 주문의 상태가 COMPLETED로 변경되고 이벤트가 발행되어야 한다`() {
        // given
        val orderId = 1L
        val pgTxId = "testPgTransactionId"
        val mockOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            status = OrderStatus.PENDING,
        )
        val command = OrderCompleteCommand(
            orderId = orderId,
            pgTransactionId = pgTxId,
            paidAmount = mockOrder.totalPrice,
        )

        given(orderQueryPort.findById(orderId))
            .willReturn(mockOrder)

        // when
        orderCommandService.completeOrder(command)

        // then
        verify(orderCommandPort).update(
            check { order ->
                order.status shouldBe OrderStatus.COMPLETED
                order.pgTransactionId shouldBe pgTxId
            },
        )
        verify(orderEventPort).publish(check<OrderCompletedEvent> { event ->
            event.orderId shouldBe orderId
            event.userId shouldBe mockOrder.userId
            event.totalAmount shouldBe mockOrder.totalPrice
            event.receiverName shouldBe mockOrder.orderReceiver.receiverName
            event.receiverPhoneNumber shouldBe mockOrder.orderReceiver.receiverPhoneNumber
            event.zipCode shouldBe mockOrder.orderAddress.zipCode
            event.baseAddress shouldBe mockOrder.orderAddress.baseAddress
            event.detailAddress shouldBe mockOrder.orderAddress.detailAddress
        })
    }

    @Test
    fun `결제 완료 Command 수신 시 PENDING이 아닌 주문은 멱등하게 스킵된다`() {
        // given
        val orderId = 1L
        val mockOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            status = OrderStatus.CANCELLED,
        )
        val command = OrderCompleteCommand(
            orderId = orderId,
            pgTransactionId = "pgTxId",
            paidAmount = mockOrder.totalPrice,
        )

        given(orderQueryPort.findById(orderId))
            .willReturn(mockOrder)

        // when
        orderCommandService.completeOrder(command)

        // then
        verify(orderCommandPort, never()).update(any())
        verify(orderEventPort, never()).publish(any())
    }

    @Test
    fun `주문 취소 Command 수신 시 주문 상태가 CANCELLED로 변경되고 업데이트되어야 한다`() {
        // given
        val orderId = 10L
        val authenticatedUserId = 3L

        val command = OrderCancelCommand(
            userId = authenticatedUserId,
            orderId = orderId,
        )

        val pendingOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            userId = authenticatedUserId,
            status = OrderStatus.PENDING,
        )

        // when
        given(orderQueryPort.findById(orderId)).willReturn(pendingOrder)

        orderCommandService.cancelOrder(command)

        // then
        verify(orderQueryPort).findById(orderId)

        verify(orderCommandPort).update(
            check { savedOrder ->
                savedOrder.id shouldBe orderId
                savedOrder.status shouldBe OrderStatus.CANCELLED
            },
        )
    }

    @Test
    fun `주문 취소 Command 수신 시 이미 CANCELLED 상태면 OrderStatusInvalidException을 던져야 한다`() {
        // given
        val orderId = 10L
        val authenticatedUserId = 3L

        val command = OrderCancelCommand(
            userId = authenticatedUserId,
            orderId = orderId,
        )

        val cancelledOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            userId = authenticatedUserId,
            status = OrderStatus.CANCELLED,
        )

        // when
        given(orderQueryPort.findById(orderId)).willReturn(cancelledOrder)

        // then
        shouldThrow<OrderStatusInvalidException> {
            orderCommandService.cancelOrder(command)
        }

        verify(orderCommandPort, never()).update(any())
    }

    @Test
    fun `다른 사용자의 주문을 취소할 경우 OrderAccessDenied예외를 던져야 한다`() {
        // given
        val orderId = 10L
        val authenticatedUserId = 3L
        val requestUserId = 2L
        val command = OrderCancelCommand(
            userId = requestUserId,
            orderId = orderId,
        )

        val pendingOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            userId = authenticatedUserId,
            status = OrderStatus.PENDING,
        )

        // when
        given(orderQueryPort.findById(orderId)).willReturn(pendingOrder)

        // then
        shouldThrow<OrderAccessDeniedException> {
            orderCommandService.cancelOrder(command)
        }

        verify(orderCommandPort, never()).update(any())
    }

    @Test
    fun `결제 실패 Command 수신 시 PENDING 주문의 상태가 FAILED로 변경되고 이벤트가 발행되어야 한다`() {
        // given
        val orderId = 1L
        val userId = 2L
        val mockOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            userId = userId,
            status = OrderStatus.PENDING,
        )
        val command = OrderFailCommand(orderId = orderId)

        given(orderQueryPort.findById(orderId))
            .willReturn(mockOrder)

        // when
        orderCommandService.failOrder(command)

        // then
        verify(orderCommandPort).update(
            check { order ->
                order.status shouldBe OrderStatus.FAILED
            },
        )
        verify(orderEventPort).publish(check<OrderFailedEvent> { event ->
            event.orderId shouldBe orderId
            event.userId shouldBe userId
            event.totalAmount shouldBe mockOrder.totalPrice
            event.products.size shouldBe mockOrder.orderProducts.size
        })
    }

    @Test
    fun `결제 실패 Command 수신 시 PENDING이 아닌 주문은 멱등하게 스킵된다`() {
        // given
        val orderId = 1L
        val mockOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            status = OrderStatus.COMPLETED,
        )
        val command = OrderFailCommand(orderId = orderId)

        given(orderQueryPort.findById(orderId))
            .willReturn(mockOrder)

        // when
        orderCommandService.failOrder(command)

        // then
        verify(orderCommandPort, never()).update(any())
        verify(orderEventPort, never()).publish(any())
    }
}
