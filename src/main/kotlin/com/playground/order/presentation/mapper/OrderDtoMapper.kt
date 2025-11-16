package com.playground.order.presentation.mapper

import com.playground.order.application.port.inbound.command.OrderCreateCommand
import com.playground.order.application.port.inbound.command.OrderProductCreateCommand
import com.playground.order.presentation.request.OrderCreateRequestDto
import com.playground.order.presentation.request.OrderProductRequestDto

fun OrderCreateRequestDto.toCommand(userId: Long): OrderCreateCommand =
    OrderCreateCommand(
        userId = userId,
        orderProducts = orderProducts.map { it.toCommand() },
    )

fun OrderProductRequestDto.toCommand(): OrderProductCreateCommand =
    OrderProductCreateCommand(
        productId = productId,
        quantity = quantity,
    )
