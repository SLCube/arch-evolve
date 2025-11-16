package com.playground.order.domain.model

import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.exception.OrderStatusInvalidException
import java.time.LocalDateTime

class Order(
    val id: Long? = null,
    val userId: Long,
    var totalPrice: Long,
    var status: OrderStatus = OrderStatus.PENDING,
    val orderProducts: MutableList<OrderProduct> = mutableListOf(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun addOrderProduct(orderProduct: OrderProduct) {
        orderProducts.add(orderProduct)
    }

    fun calculateTotalPrice() {
        this.totalPrice = orderProducts.sumOf { it.price * it.quantity }
    }

    fun completeOrder() {
        require(this.status == OrderStatus.PENDING) { "주문 상태가 PENDING일 때만 완료할 수 있습니다." }
        this.status = OrderStatus.COMPLETED
        this.updatedAt = LocalDateTime.now()
    }

    fun cancelOrder() {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.COMPLETED) {
            throw OrderStatusInvalidException(this.status)
        }
        this.status = OrderStatus.CANCELLED
        this.updatedAt = LocalDateTime.now()
    }

    override fun toString(): String =
        "Order(id=$id, userId=$userId, totalPrice=$totalPrice, status=$status, orderProductsCount=${orderProducts.size})"
}
