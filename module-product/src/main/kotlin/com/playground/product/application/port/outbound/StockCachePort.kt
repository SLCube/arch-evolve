package com.playground.product.application.port.outbound

interface StockCachePort {
    fun decreaseStock(
        productId: Long,
        quantity: Int,
    ): Long

    fun setStock(
        productId: Long,
        stock: Int,
    )

    fun setStockBatch(stockMap: Map<Long, Int>)

    fun getStock(productId: Long): Int

    fun getDirtyProductIds(): Set<Long>

    fun removeDirtyFlags(productIds: Set<Long>)
}
