package com.playground.order.domain.model

import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.exception.OrderAccessDeniedException
import com.playground.order.domain.exception.OrderStatusInvalidException
import java.math.BigDecimal
import java.time.LocalDateTime

class Order(
    val id: Long? = null,
    val userId: Long,
    var totalPrice: BigDecimal,
    var status: OrderStatus = OrderStatus.PENDING,
    val orderProducts: MutableList<OrderProduct> = mutableListOf(),
    val orderAddress: OrderAddress,
    val orderReceiver: OrderReceiver,
    var pgTransactionId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun addOrderProduct(orderProduct: OrderProduct) {
        orderProducts.add(orderProduct)
    }

    fun calculateTotalPrice() {
        this.totalPrice = orderProducts.fold(BigDecimal.ZERO) { total, orderProduct ->
            val itemAmount = orderProduct.price.multiply(orderProduct.quantity.toBigDecimal())
            total.add(itemAmount)
        }
    }

    fun completeOrder(pgTransactionId: String) {
        require(this.status == OrderStatus.PENDING) { "주문 상태가 PENDING일 때만 완료할 수 있습니다." }
        this.status = OrderStatus.COMPLETED
        this.pgTransactionId = pgTransactionId
        this.updatedAt = LocalDateTime.now()
    }

    fun cancelOrder() {
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.COMPLETED) {
            throw OrderStatusInvalidException(this.status)
        }
        this.status = OrderStatus.CANCELLED
        this.updatedAt = LocalDateTime.now()
    }

    fun validateOwner(currentUserId: Long) {
        if (this.userId != currentUserId) {
            throw OrderAccessDeniedException(this.id!!, currentUserId)
        }
    }

    override fun toString(): String =
        "Order(id=$id, userId=$userId, totalPrice=$totalPrice, status=$status, orderProductsCount=${orderProducts.size})"
}
