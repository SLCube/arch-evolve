package com.playground.order.domain.model

import com.playground.order.application.port.inbound.command.OrderCreateCommand
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.exception.OrderAccessDeniedException
import com.playground.order.domain.exception.OrderStatusInvalidException
import com.playground.product.contract.domain.vo.ProductInfo
import com.playground.user.contract.domain.vo.ReceiverAddressInfo
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
        if (this.status != OrderStatus.PENDING) {
            throw OrderStatusInvalidException(this.status)
        }
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

    companion object {
        fun createOrder(
            command: OrderCreateCommand,
            addressInfo: ReceiverAddressInfo,
            productInfoMap: Map<Long, ProductInfo>,
        ): Order {
            val order = Order(
                userId = command.userId,
                totalPrice = BigDecimal.ZERO,
                orderAddress = OrderAddress.fromAddressInfo(addressInfo),
                orderReceiver = OrderReceiver.fromAddressInfo(addressInfo),
            )

            command.orderProducts
                .map { orderProductCommand ->
                    val productInfo = productInfoMap.getValue(orderProductCommand.productId)

                    OrderProduct(
                        productId = orderProductCommand.productId,
                        quantity = orderProductCommand.quantity,
                        price = productInfo.price,
                    )
                }
                .forEach(order::addOrderProduct)

            order.calculateTotalPrice()
            return order
        }
    }
}
