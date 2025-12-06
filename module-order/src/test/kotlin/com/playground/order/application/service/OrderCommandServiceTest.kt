package com.playground.order.application.service

import com.playground.order.application.port.inbound.command.OrderCompleteCommand
import com.playground.order.application.port.outbound.OrderCommandPort
import com.playground.order.application.port.outbound.OrderEventPort
import com.playground.order.application.port.outbound.OrderQueryPort
import com.playground.order.application.provider.OrderExternalDataProvider
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.fixture.OrderTestFixture
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.jupiter.api.Test
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
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
    fun `결제 완료 Command 수신 시 주문상태가 PAID로 변경되고 PG_TXID가 기록되서 저장된다`() {

        val orderId = 1L
        val mockOrder = OrderTestFixture.mockOrder(
            id = orderId,
            status = OrderStatus.PENDING,
        )

        val pgTxId = "testPgTransactionId"
        val command = OrderCompleteCommand(
            orderId = orderId,
            pgTransactionId = pgTxId,
            paidAmount = mockOrder.totalPrice
        )

        given(orderQueryPort.findById(eq(mockOrder.id!!)))
            .willReturn(mockOrder)

        orderCommandService.completeOrder(command)

        verify(orderQueryPort).findById(mockOrder.id)
        verify(orderCommandPort).update(
            check { savedOrder ->
                savedOrder.pgTransactionId.shouldNotBeEmpty()
                savedOrder.pgTransactionId shouldBe pgTxId
                savedOrder.status shouldBe OrderStatus.COMPLETED
            }
        )
    }
}