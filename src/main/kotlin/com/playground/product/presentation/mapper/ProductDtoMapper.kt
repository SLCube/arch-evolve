package com.playground.product.presentation.mapper

import com.playground.product.application.port.`in`.command.DecreaseStockCommand
import com.playground.product.application.port.`in`.command.SaveProductCommand
import com.playground.product.application.port.`in`.command.UpdateProductCommand
import com.playground.product.presentation.request.ProductSaveRequestDto
import com.playground.product.presentation.request.ProductUpdateRequestDto

fun ProductSaveRequestDto.toCommand(): SaveProductCommand {
    return SaveProductCommand(
        name = this.name,
        stock = this.stock,
        price = this.price
    )
}

fun ProductUpdateRequestDto.toCommand(id: Long): UpdateProductCommand {
    return UpdateProductCommand(
        id = id,
        name = this.name,
        stock = this.stock,
        price = this.price
    )
}

fun toCommand(id: Long, quantity: Int): DecreaseStockCommand {
    return DecreaseStockCommand(
        id = id,
        quantity = quantity
    )
}