package com.playground.order.repository

import com.playground.order.domain.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository: JpaRepository<Order, Long>