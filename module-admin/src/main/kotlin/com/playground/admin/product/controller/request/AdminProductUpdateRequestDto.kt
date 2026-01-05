package com.playground.admin.product.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class AdminProductUpdateRequestDto(
    @field:NotBlank(message = "{admin.product.name.not-blank}")
    val name: String,
    @field:PositiveOrZero(message = "{admin.product.stock.positive-or-zero}")
    val stock: Int,
    @field:PositiveOrZero(message = "{admin.product.price.positive-or-zero}")
    val price: BigDecimal,
)
