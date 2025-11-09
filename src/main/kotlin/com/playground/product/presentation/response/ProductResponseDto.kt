package com.playground.product.presentation.response

import com.playground.product.persistence.entity.Product

data class ProductResponseDto(
    val id: Long,
    val name: String,
    val stock: Int,
    val price: Long
) {
    companion object {
        fun toResponse(product: Product): ProductResponseDto {
            val productId = requireNotNull(product.id) { "Product ID cannot be null for response creation." }
            return ProductResponseDto(
                productId,
                product.name,
                product.stock,
                product.price
            )
        }
    }
}