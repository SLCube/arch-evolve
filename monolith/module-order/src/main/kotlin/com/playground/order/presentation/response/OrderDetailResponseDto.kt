package com.playground.order.presentation.response

import com.playground.common.util.DateTimeUtils
import com.playground.order.application.service.result.OrderDetailResult
import com.playground.order.domain.enum.OrderStatus
import java.math.BigDecimal

data class OrderDetailResponseDto(
    val id: Long,
    val userId: Long,
    val totalPrice: BigDecimal,
    val status: OrderStatus,
    val orderProducts: List<OrderProductDetailResponseDto>,
    val delivery: OrderDeliveryDetailResponseDto?,
    val createdAt: String,
) {
    companion object {
        fun toResponse(result: OrderDetailResult): OrderDetailResponseDto {
            val orderProductDetails =
                result.orderProducts.map {
                    OrderProductDetailResponseDto(
                        id = it.id,
                        productId = it.productId,
                        productName = it.productName,
                        quantity = it.quantity,
                        price = it.price,
                    )
                }

            val deliveryDetail =
                result.delivery?.let {
                    OrderDeliveryDetailResponseDto(
                        deliveryId = it.deliveryId,
                        receiverName = it.receiverName,
                        receiverPhoneNumber = it.receiverPhoneNumber,
                        zipCode = it.zipCode,
                        baseAddress = it.baseAddress,
                        detailAddress = it.detailAddress,
                        deliveryStatus = it.deliveryStatus,
                    )
                }

            return OrderDetailResponseDto(
                id = result.id,
                userId = result.userId,
                totalPrice = result.totalPrice,
                status = result.status,
                orderProducts = orderProductDetails,
                delivery = deliveryDetail,
                createdAt = result.createdAt.format(DateTimeUtils.API_DATE_TIME_FORMATTER),
            )
        }
    }
}

data class OrderDeliveryDetailResponseDto(
    val deliveryId: Long,
    val receiverName: String,
    val receiverPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
    val deliveryStatus: String,
)

data class OrderProductDetailResponseDto(
    val id: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: BigDecimal,
)
