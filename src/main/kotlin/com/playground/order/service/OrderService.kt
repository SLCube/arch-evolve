package com.playground.order.service

import com.playground.order.controller.request.OrderCreateRequestDto
import com.playground.order.controller.response.OrderResponseDto
import com.playground.order.domain.Order
import com.playground.order.domain.OrderItem
import com.playground.order.repository.OrderRepository
import com.playground.product.application.port.`in`.ProductUseCase
import com.playground.product.application.port.`in`.command.DecreaseStockCommand
import com.playground.product.application.port.`in`.query.GetProductQuery
import com.playground.user.exception.UserNotFoundException
import com.playground.user.repository.UserRepository
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

        val order = Order(userId = user.id!!, totalPrice = 0)

        request.orderItems.forEach { orderItemRequest ->
            val productId = orderItemRequest.productId
            val product = productUseCase.getProduct(GetProductQuery(productId))

            val quantity = orderItemRequest.quantity
            productUseCase.decreaseStock(DecreaseStockCommand(productId, quantity))

            val orderItem = OrderItem(
                order = order,
                productId = productId,
                quantity = quantity,
                price = product.price
            )

            order.addOrderItem(orderItem)
        }

        order.calculateTotalPrice()

        val savedOrder = orderRepository.save(order)

        return OrderResponseDto.toResponse(savedOrder)
    }
}