package com.playground.product.persistence.repository

import com.playground.product.persistence.entity.QProductJpaEntity.Companion.productJpaEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.PreparedStatement

class ProductCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val jdbcTemplate: JdbcTemplate,
) : ProductCustomRepository {
    override fun decreaseStockAtomic(
        productId: Long,
        quantity: Int,
    ): Long {
        return queryFactory
            .update(productJpaEntity)
            .set(productJpaEntity.stock, productJpaEntity.stock.subtract(quantity))
            .where(
                productJpaEntity.id.eq(productId),
                productJpaEntity.stock.goe(quantity),
            ).execute()
    }

    /**
     * 배치로 여러 상품의 재고를 업데이트
     *
     * JDBC Batch UPDATE를 사용하여 성능 최적화
     */
    override fun batchUpdateStock(stockMap: Map<Long, Int>) {
        if (stockMap.isEmpty()) {
            return
        }

        val sql = "UPDATE products SET stock = ? WHERE product_id = ?"
        val entries = stockMap.entries.toList()

        jdbcTemplate.batchUpdate(
            sql,
            object : BatchPreparedStatementSetter {
                override fun setValues(
                    ps: PreparedStatement,
                    i: Int,
                ) {
                    val (productId, stock) = entries[i]
                    ps.setInt(1, stock)
                    ps.setLong(2, productId)
                }

                override fun getBatchSize(): Int = entries.size
            },
        )
    }
}
