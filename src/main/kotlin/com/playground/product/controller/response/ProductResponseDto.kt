package com.playground.product.controller.response

import com.playground.product.domain.Product

data class ProductResponseDto(
    val id: Long,
    val name: String,
    val stock: Int,
) {
    companion object {
        fun toResponse(product: Product): ProductResponseDto {
            return ProductResponseDto(
                product.id,
                product.name,
                product.stock,
            )
        }
    }
}
