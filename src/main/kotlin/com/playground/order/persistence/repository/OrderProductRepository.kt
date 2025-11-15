package com.playground.order.persistence.repository

import com.playground.order.persistence.entity.OrderProductJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderProductRepository : JpaRepository<OrderProductJpaEntity, Long>
