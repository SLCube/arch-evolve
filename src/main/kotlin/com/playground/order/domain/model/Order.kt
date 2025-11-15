package com.playground.order.domain.model

import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.exception.OrderStatusInvalidException

class Order(
    val id: Long? = null,
    val userId: Long,
    var totalPrice: Long,
    var status: OrderStatus = OrderStatus.PENDING,
    val orderProducts: MutableList<OrderProduct> = mutableListOf(),
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
    }

    fun cancelOrder() {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.COMPLETED) {
            throw OrderStatusInvalidException(this.status)
        }
        this.status = OrderStatus.CANCELLED
    }

    override fun toString(): String =
        "Order(id=$id, userId=$userId, totalPrice=$totalPrice, status=$status, orderProductsCount=${orderProducts.size})"
}
