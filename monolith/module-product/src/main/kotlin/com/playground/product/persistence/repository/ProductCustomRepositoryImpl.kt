package com.playground.product.persistence.repository

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class ProductCustomRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : ProductCustomRepository {

    override fun batchUpdateStock(stockMap: Map<Long, Int>) {
        if (stockMap.isEmpty()) {
            return
        }

        val sql = "UPDATE products SET stock = :stock WHERE product_id = :productId"
        val batchParams = stockMap.map { (productId, stock) ->
            mapOf(
                "stock" to stock,
                "productId" to productId,
            )
        }.toTypedArray()

        jdbcTemplate.batchUpdate(sql, batchParams)
    }
}
