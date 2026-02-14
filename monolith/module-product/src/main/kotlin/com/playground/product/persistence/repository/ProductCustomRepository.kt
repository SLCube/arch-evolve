package com.playground.product.persistence.repository

fun interface ProductCustomRepository {
    fun batchUpdateStock(stockMap: Map<Long, Int>)
}