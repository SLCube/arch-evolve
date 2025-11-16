package com.playground.order.persistence.adapter

import com.playground.common.application.query.PageQuery
import com.playground.common.application.query.PagedResult
import com.playground.order.application.port.out.OrderQueryPort
import com.playground.order.domain.exception.OrderNotFoundException
import com.playground.order.domain.model.Order
import com.playground.order.persistence.entity.OrderJpaEntity
import com.playground.order.persistence.mapper.toDomain
import com.playground.order.persistence.repository.OrderProductRepository
import com.playground.order.persistence.repository.OrderRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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

    override fun findOrdersByUserId(
        userId: Long,
        pageQuery: PageQuery,
    ): PagedResult<Order> {
        val direction = pageQuery.direction
        val sortBy = pageQuery.sortBy
        val sort =
            if (sortBy != null && direction != null) {
                Sort.by(Sort.Direction.valueOf(direction.uppercase()), sortBy)
            } else {
                Sort.by(Sort.Direction.DESC, "createdAt")
            }
        val pageable = PageRequest.of(pageQuery.pageNumber, pageQuery.pageSize, sort)

        val orderJpaEntitiesPage = orderRepository.findByUserId(userId, pageable)

        return mapToPagedResultOrder(orderJpaEntitiesPage)
    }

    private fun mapToPagedResultOrder(orderJpaEntitiesPage: Page<OrderJpaEntity>): PagedResult<Order> {
        val orderIdsInPage = orderJpaEntitiesPage.content.map { it.id!! }
        val orderProductsInPage =
            if (orderIdsInPage.isNotEmpty()) {
                orderProductRepository.findByOrderIdIn(orderIdsInPage)
            } else {
                emptyList()
            }

        val orderProductsMap = orderProductsInPage.groupBy { it.orderJpaEntity.id!! }

        val content =
            orderJpaEntitiesPage.content.map { orderJpaEntity ->
                val orderId = orderJpaEntity.id!!
                val orderProducts =
                    orderProductsMap
                        .getOrDefault(orderId, emptyList())
                        .map { it.toDomain() }
                        .toMutableList()
                orderJpaEntity.toDomain(orderProducts)
            }

        return PagedResult(
            content = content,
            pageNumber = orderJpaEntitiesPage.number,
            pageSize = orderJpaEntitiesPage.size,
            totalElements = orderJpaEntitiesPage.totalElements,
            totalPages = orderJpaEntitiesPage.totalPages,
        )
    }
}
