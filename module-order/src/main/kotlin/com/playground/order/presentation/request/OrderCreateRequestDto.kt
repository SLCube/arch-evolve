package com.playground.order.presentation.request

import com.playground.common.constant.ValidationConstants
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class OrderCreateRequestDto(
    @field:Size(min = ValidationConstants.ORDER_ORDERPRODUCTS_MIN_SIZE, message = "{order.orderItems.size}")
    val orderProducts: List<OrderProductRequestDto>,
    @field:NotNull(message = "{order.addressId.not-null}")
    val addressId: Long,
)

data class OrderProductRequestDto(
    @field:NotNull(message = "{orderItem.productId.not-null}")
    val productId: Long,
    @field:Min(value = ValidationConstants.ORDERPRODUCT_QUANTITY_MIN_SIZE, message = "{orderItem.quantity.min}")
    val quantity: Int,
)
