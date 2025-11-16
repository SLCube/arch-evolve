package com.playground.order.persistence.repository

import com.playground.order.persistence.entity.OrderJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<OrderJpaEntity, Long> {
    fun findByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<OrderJpaEntity>
}
