package com.playground.product.controller.response

import com.playground.product.domain.Product

data class ProductResponseDto(
    val id: Long,
    val name: String,
    val stock: Int,
) {
    companion object {
        fun toResponse(product: Product): ProductResponseDto {
            val productId = requireNotNull(product.id) { "Product ID cannot be null for response creation." }
            return ProductResponseDto(
                productId,
                product.name,
                product.stock,
            )
        }
    }
}
