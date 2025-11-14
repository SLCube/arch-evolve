package com.playground.order.application.service

import com.playground.order.application.port.`in`.OrderUseCase
import com.playground.order.application.port.`in`.command.OrderCreateCommand
import com.playground.order.application.port.out.OrderCommandPort
import com.playground.order.application.port.out.OrderEventPort
import com.playground.order.application.port.out.OrderProductQueryPort
import com.playground.order.application.port.out.OrderQueryPort
import com.playground.order.domain.event.OrderCreatedEvent
import com.playground.order.domain.exception.OrderableProductNotFoundException
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderProduct
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    private val orderCommandPort: OrderCommandPort,
    private val orderQueryPort: OrderQueryPort,
    private val orderEventPort: OrderEventPort,
    private val orderProductQueryPort: OrderProductQueryPort,
): OrderUseCase {

    override fun createOrder(command: OrderCreateCommand): Order {

        val productIds = command.orderProducts.map { it.productId }
        val productInfoMap = orderProductQueryPort.getProductInfos(productIds)

        val order = Order(
            userId = command.userId,
            totalPrice = 0
        )

        val orderProducts = command.orderProducts.map { orderProductCommand ->
            val productId = orderProductCommand.productId
            val productInfo = productInfoMap[productId]
                ?: throw OrderableProductNotFoundException(productId)

            OrderProduct(
                productId = productId,
                quantity = orderProductCommand.quantity,
                price = productInfo.price
            )
        }

        orderProducts.forEach(order::addOrderProduct)
        order.calculateTotalPrice()

        val orderProductDetails = order.orderProducts.map {
            OrderCreatedEvent.OrderProductDetail(
                productId = it.productId,
                quantity = it.quantity
            )
        }

        val savedOrder = orderCommandPort.save(order)

        val orderCreatedEvent = OrderCreatedEvent(orderProductDetails)
        orderEventPort.publish(orderCreatedEvent)

        return savedOrder
    }
}