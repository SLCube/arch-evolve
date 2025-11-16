package com.playground.order.application.validator

import com.playground.order.application.port.outbound.OrderQueryPort
import com.playground.order.domain.exception.OrderAccessDeniedException
import org.springframework.stereotype.Component

@Component
class OrderOwnerValidator(
    private val orderQueryPort: OrderQueryPort,
) {
    fun validate(
        currentUserId: Long,
        orderId: Long,
    ) {
        val order = orderQueryPort.findById(orderId)
        if (order.userId != currentUserId) {
            throw OrderAccessDeniedException(orderId, currentUserId)
        }
    }
}
