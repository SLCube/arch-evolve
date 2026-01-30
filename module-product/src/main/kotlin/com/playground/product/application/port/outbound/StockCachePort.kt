package com.playground.product.application.port.outbound

interface StockCachePort {
    // 3단계 재고 관리
    fun reserveStock(
        productId: Long,
        quantity: Int,
    ): Long

    fun confirmStock(
        productId: Long,
        quantity: Int,
    ): Long

    fun releaseReservedStock(
        productId: Long,
        quantity: Int,
    ): Long

    // 조회
    fun getAvailableStock(productId: Long): Int

    fun getReservedStock(productId: Long): Int

    fun getConfirmedStock(productId: Long): Int

    fun setStock(
        productId: Long,
        stock: Int,
    )

    fun setStockBatch(stockMap: Map<Long, Int>)

    fun getStock(productId: Long): Int

    fun getDirtyProductIds(): Set<Long>

    fun removeDirtyFlags(productIds: Set<Long>)
}
