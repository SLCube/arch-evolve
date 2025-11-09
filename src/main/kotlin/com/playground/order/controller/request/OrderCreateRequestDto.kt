package com.playground.order.controller.request

import com.playground.common.constant.ValidationConstants
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class OrderCreateRequestDto(
    @field:Valid
    @field:Size(min = ValidationConstants.ORDER_ORDERITEMS_MIN_SIZE, message = "{order.orderItems.size}")
    val orderItems: List<OrderItemRequestDto>
)

data class OrderItemRequestDto(
    @field:NotNull(message = "{orderItem.productId.not-null}")
    val productId: Long,

    @field:Min(value = ValidationConstants.ORDERITEM_QUANTITY_MIN_SIZE, message = "{orderItem.quantity.min}")
    val quantity: Int
)
