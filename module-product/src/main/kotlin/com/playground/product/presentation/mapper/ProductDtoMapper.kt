package com.playground.product.presentation.mapper

import com.playground.product.application.port.inbound.command.SaveProductCommand
import com.playground.product.application.port.inbound.command.UpdateProductCommand
import com.playground.product.presentation.request.ProductSaveRequestDto
import com.playground.product.presentation.request.ProductUpdateRequestDto

fun ProductSaveRequestDto.toCommand(): SaveProductCommand =
    SaveProductCommand(
        name = this.name,
        stock = this.stock,
        price = this.price,
    )

fun ProductUpdateRequestDto.toCommand(id: Long): UpdateProductCommand =
    UpdateProductCommand(
        id = id,
        name = this.name,
        stock = this.stock,
        price = this.price,
    )
