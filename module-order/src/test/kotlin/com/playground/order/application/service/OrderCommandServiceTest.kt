package com.playground.order.application.service

import com.playground.order.application.port.inbound.command.OrderCancelCommand
import com.playground.order.application.port.inbound.command.OrderCompleteCommand
import com.playground.order.application.port.outbound.OrderCommandPort
import com.playground.order.application.port.outbound.OrderEventPort
import com.playground.order.application.port.outbound.OrderQueryPort
import com.playground.order.application.provider.OrderExternalDataProvider
import com.playground.order.contract.domain.event.OrderCompletedEvent
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.exception.OrderAccessDeniedException
import com.playground.order.domain.exception.OrderStatusInvalidException
import com.playground.order.domain.model.Order
import com.playground.order.fixture.application.domain.AddressInfoTestFixture
import com.playground.order.fixture.application.command.OrderCommandTestFixture
import com.playground.order.fixture.application.domain.OrderDomainTestFixture
import com.playground.order.fixture.application.domain.ProductInfoTestFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@Suppress("NonAsciiCharacters")
class OrderCommandServiceTest {

    private val orderQueryPort: OrderQueryPort = mock()
    private val orderCommandPort: OrderCommandPort = mock()
    private val orderEventPort: OrderEventPort = mock()
    private val orderExternalDataProvider: OrderExternalDataProvider = mock()

    private val orderCommandService: OrderCommandService = OrderCommandService(
        orderQueryPort = orderQueryPort,
        orderCommandPort = orderCommandPort,
        orderEventPort = orderEventPort,
        orderExternalDataProvider = orderExternalDataProvider,
    )

    @Test
    fun `주문 생성 Command 수신 시 주문 Aggregate가 생성되고 Event가 발행되어야 한다`() {
        //given
        val userId = 2L
        val orderId = 1L
        val command = OrderCommandTestFixture.createOrderCommand(userId = userId)
        val mockProductInfo = ProductInfoTestFixture.mockProductInfos()
        val mockAddressInfo = AddressInfoTestFixture.mockAddressInfo()
        val mockOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            userId = userId,
        )

        given(orderExternalDataProvider.getVerifiedProductInfos(any()))
            .willReturn(mockProductInfo)
        given(orderExternalDataProvider.getAddressInfoByAddressId(eq(userId), eq(command.addressId)))
            .willReturn(mockAddressInfo)

        given(orderCommandPort.save(any<Order>()))
            .willReturn(mockOrder)

        // when
        val createdOrder = orderCommandService.createOrder(command)

        // then
        verify(orderCommandPort).save(any<Order>())

        verify(orderEventPort).publish(check<OrderCreatedEvent> { event ->
            event.orderId shouldBe orderId
            event.userId shouldBe userId
            event.totalAmount shouldBe createdOrder.totalPrice
        })

        createdOrder.id shouldBe orderId
        createdOrder.status shouldBe OrderStatus.PENDING
    }

    @Test
    fun `결제 완료 Command 수신 시 주문상태가 PAID로 변경되고 PG_TXID가 기록되서 저장된다`() {
        // given
        val orderId = 1L
        val mockOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            status = OrderStatus.PENDING,
        )

        val pgTxId = "testPgTransactionId"
        val command = OrderCompleteCommand(
            orderId = orderId,
            pgTransactionId = pgTxId,
            paidAmount = mockOrder.totalPrice
        )

        // when
        given(orderQueryPort.findById(eq(mockOrder.id!!)))
            .willReturn(mockOrder)

        given(orderCommandPort.update(any<Order>()))
            .willAnswer { invocation ->
                invocation.arguments[0] as Order
            }

        val completeOrder = orderCommandService.completeOrder(command)

        // then
        verify(orderQueryPort).findById(mockOrder.id)
        verify(orderCommandPort).update(
            check { savedOrder ->
                savedOrder.pgTransactionId.shouldNotBeEmpty()
                savedOrder.pgTransactionId shouldBe pgTxId
                savedOrder.status shouldBe OrderStatus.COMPLETED
            }
        )

        verify(orderEventPort).publish(check<OrderCompletedEvent> { event ->
            event.orderId shouldBe orderId
            event.userId shouldBe completeOrder.userId
            event.totalAmount shouldBe completeOrder.totalPrice
            event.receiverName shouldBe mockOrder.orderReceiver.receiverName
            event.receiverPhoneNumber shouldBe mockOrder.orderReceiver.receiverPhoneNumber
            event.zipCode shouldBe mockOrder.orderAddress.zipCode
            event.baseAddress shouldBe mockOrder.orderAddress.baseAddress
            event.detailAddress shouldBe mockOrder.orderAddress.detailAddress
        })
    }

    @Test
    fun `결제 완료 Command 수신 시 주문상태가 PENDING이 아니라면 OrderStatusValidException을 던진다`() {
        // given
        val orderId = 1L
        val mockOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            status = OrderStatus.CANCELLED
        )

        val pgTxId = "testPgTransactionId"
        val command = OrderCompleteCommand(
            orderId = orderId,
            pgTransactionId = pgTxId,
            paidAmount = OrderDomainTestFixture.mockOrder().totalPrice
        )

        // when
        given(orderQueryPort.findById(eq(orderId)))
            .willReturn(mockOrder)

        // then
        shouldThrow<OrderStatusInvalidException> {
            orderCommandService.completeOrder(command)
        }

        verify(orderCommandPort, never()).update(any())
    }

    @Test
    fun `주문 취소 Command 수신 시 주문 상태가 CANCELLED로 변경되고 업데이트되어야 한다`() {
        // given
        val orderId = 10L
        val authenticatedUserId = 3L

        val command = OrderCancelCommand(
            userId = authenticatedUserId,
            orderId = orderId
        )

        val pendingOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            userId = authenticatedUserId,
            status = OrderStatus.PENDING
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
            }
        )
    }

    @Test
    fun `주문 취소 Command 수신 시 이미 CANCELLED 상태면 OrderStatusInvalidException을 던져야 한다`() {
        // given
        val orderId = 10L
        val authenticatedUserId = 3L

        val command = OrderCancelCommand(
            userId = authenticatedUserId,
            orderId = orderId
        )

        val cancelledOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            userId = authenticatedUserId,
            status = OrderStatus.CANCELLED
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
            status = OrderStatus.PENDING
        )

        // when
        given(orderQueryPort.findById(orderId)).willReturn(pendingOrder)

        // then
        shouldThrow<OrderAccessDeniedException> {
            orderCommandService.cancelOrder(command)
        }

        verify(orderCommandPort, never()).update(any())
    }
}
