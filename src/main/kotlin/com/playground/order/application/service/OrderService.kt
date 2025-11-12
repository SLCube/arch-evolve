package com.playground.order.application.service

import com.playground.order.persistence.entity.OrderJpaEntity
import com.playground.order.persistence.entity.OrderProductJpaEntity
import com.playground.order.presentation.request.OrderCreateRequestDto
import com.playground.order.presentation.response.OrderResponseDto
import com.playground.order.persistence.repository.OrderRepository
import com.playground.product.application.port.`in`.ProductUseCase
import com.playground.product.application.port.`in`.command.DecreaseStockCommand
import com.playground.product.application.port.`in`.query.GetProductQuery
import com.playground.user.domain.exception.UserNotFoundException
import com.playground.user.persistence.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val productUseCase: ProductUseCase
) {

    fun createOrder(loginId: String, request: OrderCreateRequestDto): OrderResponseDto {
        val user = userRepository.findByLoginId(loginId).orElseThrow { UserNotFoundException() }

        val orderJpaEntity = OrderJpaEntity(userId = user.id!!, totalPrice = 0)

        request.orderItems.forEach { orderItemRequest ->
            val productId = orderItemRequest.productId
            val product = productUseCase.getProduct(GetProductQuery(productId))

            val quantity = orderItemRequest.quantity
            productUseCase.decreaseStock(DecreaseStockCommand(productId, quantity))

            val orderProductJpaEntity = OrderProductJpaEntity(
                orderJpaEntity = orderJpaEntity,
                productId = productId,
                quantity = quantity,
                price = product.price
            )

            orderJpaEntity.addOrderItem(orderProductJpaEntity)
        }

        orderJpaEntity.calculateTotalPrice()

        val savedOrder = orderRepository.save(orderJpaEntity)

        return OrderResponseDto.Companion.toResponse(savedOrder)
    }
}