package com.playground.product.presentation.mapper

import com.playground.product.application.port.inbound.command.ProductSaveCommand
import com.playground.product.application.port.inbound.command.ProductUpdateCommand
import com.playground.product.presentation.request.ProductSaveRequestDto
import com.playground.product.presentation.request.ProductUpdateRequestDto

fun ProductSaveRequestDto.toCommand(): ProductSaveCommand =
    ProductSaveCommand(
        name = this.name,
        stock = this.stock,
        price = this.price,
    )

fun ProductUpdateRequestDto.toCommand(id: Long): ProductUpdateCommand =
    ProductUpdateCommand(
        id = id,
        name = this.name,
        stock = this.stock,
        price = this.price,
    )
