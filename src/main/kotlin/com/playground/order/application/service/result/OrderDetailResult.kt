package com.playground.order.application.service.result

import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.model.Order
import com.playground.order.domain.vo.ProductInfo
import java.time.LocalDateTime

data class OrderDetailResult(
    val id: Long,
    val userId: Long,
    val totalPrice: Long,
    val status: OrderStatus,
    val orderProducts: List<OrderProductDetail>,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun of(
            order: Order,
            productInfoMap: Map<Long, ProductInfo>,
        ): OrderDetailResult {
            val orderProductDetails =
                order.orderProducts.map { orderProduct ->
                    val productInfo = productInfoMap.getValue(orderProduct.productId)
                    OrderProductDetail(
                        id = orderProduct.id!!,
                        productId = orderProduct.productId,
                        productName = productInfo.productName,
                        quantity = orderProduct.quantity,
                        price = orderProduct.price,
                    )
                }

            return OrderDetailResult(
                id = order.id!!,
                userId = order.userId,
                totalPrice = order.totalPrice,
                status = order.status,
                orderProducts = orderProductDetails,
                createdAt = order.createdAt,
            )
        }
    }
}

data class OrderProductDetail(
    val id: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: Long,
)
