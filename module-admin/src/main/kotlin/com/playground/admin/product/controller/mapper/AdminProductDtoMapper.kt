package com.playground.admin.product.controller.mapper

import com.playground.admin.product.controller.request.AdminProductSaveRequestDto
import com.playground.admin.product.controller.request.AdminProductUpdateRequestDto
import com.playground.admin.product.service.command.AdminProductSaveCommand
import com.playground.admin.product.service.command.AdminProductUpdateCommand

fun AdminProductSaveRequestDto.toCommand(): AdminProductSaveCommand =
    AdminProductSaveCommand(
        name = this.name,
        stock = this.stock,
        price = this.price,
    )

fun AdminProductUpdateRequestDto.toCommand(id: Long): AdminProductUpdateCommand =
    AdminProductUpdateCommand(
        id = id,
        name = this.name,
        stock = this.stock,
        price = this.price,
    )
