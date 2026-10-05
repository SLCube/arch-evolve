package com.playground.order.contract.port

/** DB에서 주문이 조회되지 않으면 UNKNOWN을 반환한다. 부재를 롤백으로 단정하지 않는다. */
fun interface OrderStockRecoveryPort {
    fun inspect(orderId: Long): OrderStockOutcome
}

enum class OrderStockOutcome { PENDING, CONFIRMED, RELEASED, UNKNOWN }
