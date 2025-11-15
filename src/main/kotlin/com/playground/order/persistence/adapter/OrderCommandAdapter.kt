package com.playground.order.persistence.adapter

import com.playground.order.application.port.out.OrderCommandPort
import com.playground.order.domain.exception.OrderNotFoundException
import com.playground.order.domain.model.Order
import com.playground.order.persistence.entity.OrderJpaEntity
import com.playground.order.persistence.entity.OrderProductJpaEntity
import com.playground.order.persistence.mapper.toDomain
import com.playground.order.persistence.repository.OrderProductRepository
import com.playground.order.persistence.repository.OrderRepository
import org.springframework.stereotype.Component

@Component
class OrderCommandAdapter(
    private val orderRepository: OrderRepository,
    private val orderProductRepository: OrderProductRepository,
) : OrderCommandPort {
    override fun save(order: Order): Order {
        val orderJpaEntity = OrderJpaEntity.toJpaEntity(order)
        val savedOrderJpaEntity = orderRepository.save(orderJpaEntity)

        val orderProductJpaEntities =
            order.orderProducts.map { orderProduct ->
                OrderProductJpaEntity.toJpaEntity(orderProduct, savedOrderJpaEntity)
            }

        val savedOrderProductJpaEntities = orderProductRepository.saveAll(orderProductJpaEntities)

        val orderProducts =
            savedOrderProductJpaEntities.map { orderProductJpaEntity ->
                orderProductJpaEntity.toDomain()
            }

        return savedOrderJpaEntity.toDomain(orderProducts.toMutableList())
    }

    override fun update(order: Order): Order {
        val orderId = order.id!!

        val orderJpaEntity =
            orderRepository
                .findById(orderId)
                .orElseThrow { OrderNotFoundException(orderId) }

        orderJpaEntity.updateFromDomain(order)

        val orderProductJpaEntities = orderProductRepository.findByOrderId(orderId)
        val orderProducts = orderProductJpaEntities.map { it.toDomain() }.toMutableList()

        return orderJpaEntity.toDomain(orderProducts)
    }
}
