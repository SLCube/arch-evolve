package com.playground.order.application.service.result

import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.model.Order
import com.playground.product.contract.domain.vo.ProductInfo
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderSummaryResult(
    val id: Long,
    val representativeProductName: String,
    val totalPrice: BigDecimal,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun of(
            order: Order,
            productInfoMap: Map<Long, ProductInfo>,
        ): OrderSummaryResult {
            val representativeProductName = order.orderProducts.firstOrNull()?.let { firstProduct ->
                val firstProductId = firstProduct.id
                val firstProductName = productInfoMap[firstProductId]?.productName ?: "알 수 없는 상품"
                if (order.orderProducts.size > 1) {
                    "$firstProductName 외 ${order.orderProducts.size - 1}건"
                } else {
                    firstProductName
                }
            } ?: ""

            return OrderSummaryResult(
                id = order.id!!,
                representativeProductName = representativeProductName,
                totalPrice = order.totalPrice,
                status = order.status,
                createdAt = order.createdAt,
            )
        }
    }
}
