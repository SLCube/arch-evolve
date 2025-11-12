package com.playground.order.presentation.response

import com.playground.order.persistence.entity.OrderJpaEntity
import com.playground.order.persistence.entity.OrderProductJpaEntity
import com.playground.order.domain.enum.OrderStatus
import java.time.LocalDateTime

data class OrderResponseDto(
    val id: Long,
    val userId: Long,
    val totalPrice: Long,
    val status: OrderStatus,
    val orderDate: LocalDateTime,
) {
    companion object {
        fun toResponse(orderJpaEntity: OrderJpaEntity): OrderResponseDto {
            return OrderResponseDto(
                id = orderJpaEntity.id!!,
                userId = orderJpaEntity.userId,
                totalPrice = orderJpaEntity.totalPrice,
                status = orderJpaEntity.status,
                orderDate = orderJpaEntity.createdAt
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
        fun toResponse(orderProductJpaEntity: OrderProductJpaEntity): OrderItemResponseDto {
            return OrderItemResponseDto(
                id = orderProductJpaEntity.id!!,
                productId = orderProductJpaEntity.productId,
                quantity = orderProductJpaEntity.quantity,
                price = orderProductJpaEntity.price
            )
        }
    }
}
