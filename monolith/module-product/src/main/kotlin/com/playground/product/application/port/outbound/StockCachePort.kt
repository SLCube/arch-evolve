package com.playground.product.application.port.outbound

interface StockCachePort {
    /** 모든 상품을 예약하며, 재고 부족 시 변경 없이 해당 상품 ID를 반환한다. 성공 시 null. */
    fun reserveStocks(
        orderId: Long,
        quantitiesByProductId: Map<Long, Int>,
    ): Long?

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

    /**
     * dirty set의 모든 productId를 원자적으로 읽고 삭제한다.
     */
    fun getDirtyProductIdsAndClear(): Set<Long>
}
