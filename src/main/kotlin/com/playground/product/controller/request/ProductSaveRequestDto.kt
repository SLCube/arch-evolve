package com.playground.product.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

data class ProductSaveRequestDto(
    @field:NotBlank(message = "상품 이름은 필수입니다.")
    val name: String,
    @field:PositiveOrZero(message = "재고량은 0보다 커야 합니다.")
    val stock: Int,
)
