package com.playground.order.application.port.`in`

import com.playground.order.application.service.result.OrderDetailResult

fun interface OrderQueryUseCase {
    fun getOrder(
        userId: Long,
        orderId: Long,
    ): OrderDetailResult
}
