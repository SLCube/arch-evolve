package com.playground.order.persistence.adapter

import com.playground.order.application.port.out.OrderQueryPort
import com.playground.order.domain.exception.OrderNotFoundException
import com.playground.order.domain.model.Order
import com.playground.order.persistence.mapper.toDomain
import com.playground.order.persistence.repository.OrderProductRepository
import com.playground.order.persistence.repository.OrderRepository
import org.springframework.stereotype.Component

@Component
class OrderQueryAdapter(
    private val orderRepository: OrderRepository,
    private val orderProductRepository: OrderProductRepository,
) : OrderQueryPort {
    override fun findById(orderId: Long): Order {
        val orderProductJpaEntities = orderProductRepository.findByOrderId(orderId)

        val orderProducts = orderProductJpaEntities.map { it.toDomain() }.toMutableList()

        return orderRepository
            .findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }
            .toDomain(orderProducts)
    }
}
