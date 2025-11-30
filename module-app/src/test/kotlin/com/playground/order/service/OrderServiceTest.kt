package com.playground.order.service

import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderCompleteCommand
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.persistence.entity.OrderJpaEntity
import com.playground.order.persistence.repository.OrderRepository
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@Suppress("NonAsciiCharacters")
@SpringBootTest
class OrderServiceTest(
    @param:Autowired private val orderRepository: OrderRepository,
    @param:Autowired private val orderCommandUseCase: OrderCommandUseCase,
) {

    @Test
    fun `결제 완료 이벤트 수신 시 주문 상태가 PAID로 변경되고 PG_TXID가 기록된다`() {
        val savedOrder = orderRepository.save(
            OrderJpaEntity(
                userId = 1L,
                totalPrice = 10000L.toBigDecimal(),
                status = OrderStatus.PENDING,
            )
        )

        orderCommandUseCase.completeOrder(
            OrderCompleteCommand(
                orderId = savedOrder.id!!,
                pgTransactionId = "testPgTransactionId",
                paidAmount = 10000L.toBigDecimal(),
            )
        )

        val completedOrder = orderRepository.findById(savedOrder.id!!).get()

        completedOrder.pgTransactionId.shouldNotBeNull()
        completedOrder.status shouldBe OrderStatus.COMPLETED
    }
}