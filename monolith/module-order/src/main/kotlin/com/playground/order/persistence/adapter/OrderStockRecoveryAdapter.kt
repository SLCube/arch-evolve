package com.playground.order.persistence.adapter

import com.playground.order.contract.port.OrderStockOutcome
import com.playground.order.contract.port.OrderStockRecoveryPort
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.persistence.repository.OrderRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class OrderStockRecoveryAdapter(
    private val orders: OrderRepository,
) : OrderStockRecoveryPort {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun inspect(orderId: Long): OrderStockOutcome {
        // 조회되지 않는 주문은 생성 중일 수도 있으므로 예약을 해제하지 않는다.
        val order = orders.findById(orderId).orElse(null) ?: return OrderStockOutcome.UNKNOWN
        return when (order.status) {
            OrderStatus.PENDING -> OrderStockOutcome.PENDING
            OrderStatus.COMPLETED -> OrderStockOutcome.CONFIRMED
            OrderStatus.FAILED, OrderStatus.CANCELLED -> OrderStockOutcome.RELEASED
        }
    }
}
