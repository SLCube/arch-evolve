package com.playground.order.application.service

import com.playground.order.application.port.`in`.OrderUseCase
import com.playground.order.application.port.`in`.command.OrderCreateCommand
import com.playground.order.application.port.out.OrderCommandPort
import com.playground.order.application.port.out.OrderEventPort
import com.playground.order.application.port.out.OrderQueryPort
import com.playground.order.application.provider.OrderProductProvider
import com.playground.order.domain.event.OrderCreatedEvent
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderProduct
import com.playground.order.domain.vo.ProductInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    private val orderCommandPort: OrderCommandPort,
    private val orderQueryPort: OrderQueryPort,
    private val orderEventPort: OrderEventPort,
    private val orderProductProvider: OrderProductProvider,
) : OrderUseCase {
    override fun createOrder(command: OrderCreateCommand): Order {
        val productInfoMap = orderProductProvider.getVerifiedProductInfos(command)
        val order = createOrderAggregate(command, productInfoMap)

        val savedOrder = orderCommandPort.save(order)

        publishOrderCreationEvent(savedOrder)

        return savedOrder
    }

    private fun createOrderAggregate(
        command: OrderCreateCommand,
        productInfoMap: Map<Long, ProductInfo>,
    ): Order {
        val order =
            Order(
                userId = command.userId,
                totalPrice = 0,
            )

        val orderProducts =
            command.orderProducts.map { orderProductCommand ->
                val productInfo = productInfoMap.getValue(orderProductCommand.productId)

                OrderProduct(
                    productId = orderProductCommand.productId,
                    quantity = orderProductCommand.quantity,
                    price = productInfo.price,
                )
            }

        orderProducts.forEach(order::addOrderProduct)
        order.calculateTotalPrice()
        return order
    }

    private fun publishOrderCreationEvent(order: Order) {
        val orderProductDetails =
            order.orderProducts.map {
                OrderCreatedEvent.OrderProductDetail(
                    productId = it.productId,
                    quantity = it.quantity,
                )
            }

        val orderCreatedEvent =
            OrderCreatedEvent(
                orderId = requireNotNull(order.id),
                userId = order.userId,
                orderProductDetails,
            )
        orderEventPort.publish(orderCreatedEvent)
    }
}
