package com.playground.order.persistence.repository

import com.playground.order.persistence.entity.OrderJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository: JpaRepository<OrderJpaEntity, Long>