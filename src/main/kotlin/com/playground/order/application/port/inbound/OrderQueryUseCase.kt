package com.playground.order.application.port.inbound

import com.playground.common.application.query.PageQuery
import com.playground.common.application.query.PagedResult
import com.playground.order.application.service.result.OrderDetailResult
import com.playground.order.application.service.result.OrderSummaryResult

interface OrderQueryUseCase {
    fun getOrder(
        userId: Long,
        orderId: Long,
    ): OrderDetailResult

    fun getOrders(
        userId: Long,
        query: PageQuery,
    ): PagedResult<OrderSummaryResult>
}
