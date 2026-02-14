package com.playground.order.presentation.response

import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderProduct
import java.math.BigDecimal

data class OrderResponseDto(
    val id: Long,
    val userId: Long,
    val totalPrice: BigDecimal,
    val status: OrderStatus,
    val orderProducts: List<OrderProductResponseDto>,
) {
    companion object {
        fun toResponse(order: Order): OrderResponseDto =
            OrderResponseDto(
                id = order.id!!,
                userId = order.userId,
                totalPrice = order.totalPrice,
                status = order.status,
                orderProducts = order.orderProducts.map { OrderProductResponseDto.toResponse(it) },
            )
    }
}

data class OrderProductResponseDto(
    val id: Long,
    val productId: Long,
    val quantity: Int,
    val price: BigDecimal,
) {
    companion object {
        fun toResponse(orderProduct: OrderProduct): OrderProductResponseDto =
            OrderProductResponseDto(
                id = orderProduct.id!!,
                productId = orderProduct.productId,
                quantity = orderProduct.quantity,
                price = orderProduct.price,
            )
    }
}
