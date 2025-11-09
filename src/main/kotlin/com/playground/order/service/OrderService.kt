package com.playground.order.service

import com.playground.order.controller.request.OrderCreateRequestDto
import com.playground.order.controller.response.OrderResponseDto
import com.playground.order.domain.Order
import com.playground.order.domain.OrderItem
import com.playground.order.repository.OrderRepository
import com.playground.product.exception.ProductNotFoundException
import com.playground.product.repository.ProductRepository
import com.playground.product.service.ProductService
import com.playground.user.exception.UserNotFoundException
import com.playground.user.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val productService: ProductService,
    private val productRepository: ProductRepository,
) {

    fun createOrder(loginId: String, request: OrderCreateRequestDto): OrderResponseDto {
        val user = userRepository.findByLoginId(loginId).orElseThrow { UserNotFoundException() }

        val order = Order(userId = user.id!!, totalPrice = 0)

        request.orderItems.forEach { orderItemRequest ->
            val productId = orderItemRequest.productId
            val product = productRepository.findById(productId)
                .orElseThrow { ProductNotFoundException(productId) }

            val quantity = orderItemRequest.quantity
            productService.decreaseStock(productId, quantity)

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