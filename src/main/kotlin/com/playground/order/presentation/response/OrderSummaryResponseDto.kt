package com.playground.order.presentation.response

import com.playground.order.application.service.result.OrderSummaryResult
import com.playground.order.domain.enum.OrderStatus
import java.time.LocalDateTime

data class OrderSummaryResponseDto(
    val id: Long,
    val representativeProductName: String,
    val totalPrice: Long,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun of(result: OrderSummaryResult): OrderSummaryResponseDto =
            OrderSummaryResponseDto(
                id = result.id,
                representativeProductName = result.representativeProductName,
                totalPrice = result.totalPrice,
                status = result.status,
                createdAt = result.createdAt,
            )
    }
}
