package com.playground.order.presentation.response

import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderProduct

data class OrderResponseDto(
    val id: Long,
    val userId: Long,
    val totalPrice: Long,
    val status: OrderStatus,
) {
    companion object {
        fun toResponse(order: Order): OrderResponseDto {
            return OrderResponseDto(
                id = order.id!!,
                userId = order.userId,
                totalPrice = order.totalPrice,
                status = order.status
            )
        }
    }
}

data class OrderItemResponseDto(
    val id: Long,
    val productId: Long,
    val quantity: Int,
    val price: Long
) {
    companion object {
        fun toResponse(orderProduct: OrderProduct): OrderItemResponseDto {
            return OrderItemResponseDto(
                id = orderProduct.id!!,
                productId = orderProduct.productId,
                quantity = orderProduct.quantity,
                price = orderProduct.price
            )
        }
    }
}
