package com.playground.product.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

data class ProductUpdateRequestDto(
    @field:NotBlank(message = "{product.name.not-blank}")
    val name: String,
    @field:PositiveOrZero(message = "{product.stock.positive-or-zero}")
    val stock: Int,
    @field:PositiveOrZero(message = "{product.price.positive-or-zero}")
    val price: Long
)
