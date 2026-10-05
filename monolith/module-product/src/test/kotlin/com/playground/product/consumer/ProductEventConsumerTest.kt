package com.playground.product.consumer

import com.playground.order.contract.domain.event.OrderCompletedEvent
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.contract.domain.event.OrderFailedEvent
import com.playground.product.application.port.inbound.StockUseCase
import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.StockConfirmCommand
import com.playground.product.application.port.inbound.command.StockReleaseCommand
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
class ProductEventConsumerTest {
    private val stockUseCase: StockUseCase = mock()
    private val consumer = ProductEventConsumer(stockUseCase)
    private val orderId = 123L
    private val occurredAt = LocalDateTime.of(2026, 10, 5, 12, 0)

    @Test
    fun `주문 생성 이벤트의 주문 ID와 상품 목록을 재고 예약에 전달한다`() {
        val event =
            OrderCreatedEvent(
                eventId = UUID.randomUUID(),
                orderId = orderId,
                userId = 7L,
                products =
                    listOf(
                        OrderCreatedEvent.OrderProductDetail(productId = 11L, quantity = 3),
                        OrderCreatedEvent.OrderProductDetail(productId = 22L, quantity = 7),
                    ),
                totalAmount = 10000.toBigDecimal(),
                occurredAt = occurredAt,
            )

        consumer.handleOrderCreatedEvent(event)

        verify(stockUseCase).decreaseStocks(
            orderId = orderId,
            commands = listOf(DecreaseStockCommand(11L, 3), DecreaseStockCommand(22L, 7)),
        )
        verifyNoMoreInteractions(stockUseCase)
    }

    @Test
    fun `주문 완료 이벤트의 주문 ID와 상품 목록을 재고 확정에 전달한다`() {
        val event =
            OrderCompletedEvent(
                eventId = UUID.randomUUID(),
                orderId = orderId,
                userId = 7L,
                products =
                    listOf(
                        OrderCompletedEvent.OrderProductDetail(productId = 11L, quantity = 3),
                        OrderCompletedEvent.OrderProductDetail(productId = 22L, quantity = 7),
                    ),
                totalAmount = 10000.toBigDecimal(),
                receiverName = "테스트 수령인",
                receiverPhoneNumber = "01000000000",
                zipCode = "12345",
                baseAddress = "테스트 주소",
                detailAddress = "101호",
                occurredAt = occurredAt,
            )

        consumer.handleOrderCompletedEvent(event)

        verify(stockUseCase).confirmStocks(
            orderId = orderId,
            commands = listOf(StockConfirmCommand(11L, 3), StockConfirmCommand(22L, 7)),
        )
        verifyNoMoreInteractions(stockUseCase)
    }

    @Test
    fun `주문 실패 이벤트의 주문 ID와 상품 목록을 예약 해제에 전달한다`() {
        val event =
            OrderFailedEvent(
                eventId = UUID.randomUUID(),
                orderId = orderId,
                userId = 7L,
                products =
                    listOf(
                        OrderFailedEvent.OrderProductDetail(productId = 11L, quantity = 3),
                        OrderFailedEvent.OrderProductDetail(productId = 22L, quantity = 7),
                    ),
                totalAmount = 10000.toBigDecimal(),
                occurredAt = occurredAt,
            )

        consumer.handleOrderFailedEvent(event)

        verify(stockUseCase).releaseReservedStocks(
            orderId = orderId,
            commands = listOf(StockReleaseCommand(11L, 3), StockReleaseCommand(22L, 7)),
        )
        verifyNoMoreInteractions(stockUseCase)
    }
}
