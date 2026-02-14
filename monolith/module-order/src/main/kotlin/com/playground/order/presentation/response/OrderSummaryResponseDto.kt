package com.playground.order.presentation.response

import com.playground.common.util.DateTimeUtils
import com.playground.order.application.service.result.OrderSummaryResult
import com.playground.order.domain.enum.OrderStatus
import java.math.BigDecimal

data class OrderSummaryResponseDto(
    val id: Long,
    val representativeProductName: String,
    val totalPrice: BigDecimal,
    val status: OrderStatus,
    val createdAt: String,
) {
    companion object {
        private const val DEFAULT_PRODUCT_NAME = "주문 상품 없음"

        fun of(result: OrderSummaryResult): OrderSummaryResponseDto {
            val name = result.representativeProductName.takeIf { it.isNotBlank() } ?: DEFAULT_PRODUCT_NAME

            return OrderSummaryResponseDto(
                id = result.id,
                representativeProductName = name,
                totalPrice = result.totalPrice,
                status = result.status,
                createdAt = result.createdAt.format(DateTimeUtils.API_DATE_TIME_FORMATTER),
            )
        }

    }
}
