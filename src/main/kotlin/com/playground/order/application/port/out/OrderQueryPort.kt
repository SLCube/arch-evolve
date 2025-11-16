package com.playground.order.application.port.out

import com.playground.common.application.query.PageQuery
import com.playground.common.application.query.PagedResult
import com.playground.order.domain.model.Order

interface OrderQueryPort {
    fun findById(orderId: Long): Order

    fun findOrdersByUserId(
        userId: Long,
        pageQuery: PageQuery,
    ): PagedResult<Order>
}
