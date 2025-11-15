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
            val productId = domain.id!!
            return ProductResponseDto(
                productId,
                domain.name,
                domain.stock,
                domain.price,
            )
        }
    }
}
