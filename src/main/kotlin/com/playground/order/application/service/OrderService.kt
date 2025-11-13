package com.playground.order.application.service

import com.playground.order.application.port.`in`.OrderUseCase
import com.playground.order.application.port.`in`.command.OrderCreateCommand
import com.playground.order.application.port.out.OrderCommandPort
import com.playground.order.application.port.out.OrderEventPort
import com.playground.order.application.port.out.OrderQueryPort
import com.playground.order.domain.event.OrderCreatedEvent
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderProduct
import com.playground.product.application.port.`in`.ProductUseCase
import com.playground.product.application.port.`in`.query.GetProductQuery
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    private val orderCommandPort: OrderCommandPort,
    private val orderQueryPort: OrderQueryPort,
    private val orderEventPort: OrderEventPort,
    private val productUseCase: ProductUseCase
): OrderUseCase {

    override fun createOrder(command: OrderCreateCommand): Order {
        val order = Order(
            userId = command.userId,
            totalPrice = 0
        )

        command.orderProducts.forEach { orderProductCommand ->
            val productId = orderProductCommand.productId
            val quantity = orderProductCommand.quantity

            val product = productUseCase.getProduct(GetProductQuery(productId))

            val orderProduct = OrderProduct(
                productId = productId,
                quantity = quantity,
                price = product.price
            )

            order.addOrderProduct(orderProduct)
        }

        order.calculateTotalPrice()

        val orderProductDetails = order.orderProducts.map {
            OrderCreatedEvent.OrderProductDetail(
                productId = it.productId,
                quantity = it.quantity
            )
        }

        val orderCreatedEvent = OrderCreatedEvent(orderProductDetails)
        orderEventPort.publish(orderCreatedEvent)

        return orderCommandPort.save(order)
    }
}