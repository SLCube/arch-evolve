package com.playground.order.controller.response

import com.playground.order.domain.Order
import com.playground.order.domain.OrderItem
import com.playground.order.enum.OrderStatus
import java.time.LocalDateTime

data class OrderResponseDto(
    val id: Long,
    val userId: Long,
    val totalPrice: Long,
    val status: OrderStatus,
    val orderDate: LocalDateTime,
    val orderItems: List<OrderItemResponseDto>
) {
    companion object {
        fun toResponse(order: Order): OrderResponseDto {
            return OrderResponseDto(
                id = order.id!!,
                userId = order.userId,
                totalPrice = order.totalPrice,
                status = order.status,
                orderDate = order.createdAt,
                orderItems = order.orderItems.map { OrderItemResponseDto.toResponse(it) }
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
        fun toResponse(orderItem: OrderItem): OrderItemResponseDto {
            return OrderItemResponseDto(
                id = orderItem.id!!,
                productId = orderItem.productId,
                quantity = orderItem.quantity,
                price = orderItem.price
            )
        }
    }
}
