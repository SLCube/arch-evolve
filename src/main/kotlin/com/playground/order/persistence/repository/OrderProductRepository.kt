package com.playground.order.persistence.repository

import com.playground.order.persistence.entity.OrderProductJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OrderProductRepository : JpaRepository<OrderProductJpaEntity, Long> {
    @Query("SELECT op FROM OrderProductJpaEntity op WHERE op.orderJpaEntity.id = :orderId")
    fun findByOrderId(orderId: Long): MutableList<OrderProductJpaEntity>

    @Query("SELECT op FROM OrderProductJpaEntity op WHERE op.orderJpaEntity.id IN :orderIds")
    fun findByOrderIdIn(orderIds: List<Long>): List<OrderProductJpaEntity>
}
