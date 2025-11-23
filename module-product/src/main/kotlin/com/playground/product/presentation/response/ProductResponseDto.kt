package com.playground.product.presentation.response

import com.playground.product.domain.model.Product
import java.math.BigDecimal

data class ProductResponseDto(
    val id: Long,
    val name: String,
    val stock: Int,
    val price: BigDecimal,
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
