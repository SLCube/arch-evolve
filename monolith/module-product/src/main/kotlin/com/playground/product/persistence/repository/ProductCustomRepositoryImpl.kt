package com.playground.product.persistence.repository

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class ProductCustomRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : ProductCustomRepository {

    override fun batchUpdateStock(stockMap: Map<Long, Int>) {
        if (stockMap.isEmpty()) {
            return
        }

        // 확정 수량이 누적되는 현재 모델에서는 동기화 재고가 단조 감소한다. 재입고는 별도 설계가 필요하다.
        val sql = "UPDATE products SET stock = :stock WHERE product_id = :productId AND stock >= :stock"
        val batchParams = stockMap.map { (productId, stock) ->
            mapOf(
                "stock" to stock,
                "productId" to productId,
            )
        }.toTypedArray()

        jdbcTemplate.batchUpdate(sql, batchParams)
    }
}
