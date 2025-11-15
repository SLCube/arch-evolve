package com.playground.product.presentation.response

import com.playground.product.domain.Product

data class ProductResponseDto(
    val id: Long,
    val name: String,
    val stock: Int,
    val price: Long,
) {
    companion object {
        fun toResponse(domain: Product): ProductResponseDto {
            val productId = requireNotNull(domain.id) { "Product ID cannot be null for response creation." }
            return ProductResponseDto(
                productId,
                domain.name,
                domain.stock,
                domain.price,
            )
        }
    }
}
