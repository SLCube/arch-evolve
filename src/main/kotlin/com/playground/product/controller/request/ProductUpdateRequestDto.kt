package com.playground.product.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

data class ProductUpdateRequestDto(
    @field:NotBlank(message = "{product.name.not-blank}")
    val name: String,
    @field:PositiveOrZero(message = "{product.stock.positive-or-zero}")
    val stock: Int,
)
