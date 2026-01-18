package com.playground.product.persistence.repository

interface ProductCustomRepository {
    fun decreaseStockAtomic(productId: Long, quantity: Int): Long
}