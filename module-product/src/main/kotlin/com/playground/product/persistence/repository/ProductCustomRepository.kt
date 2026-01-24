package com.playground.product.persistence.repository

interface ProductCustomRepository {
    fun decreaseStockAtomic(
        productId: Long,
        quantity: Int,
    ): Long

    /**
     * 배치로 여러 상품의 재고를 업데이트
     *
     * @param stockMap 상품 ID -> 재고 수량 맵
     */
    fun batchUpdateStock(stockMap: Map<Long, Int>)
}