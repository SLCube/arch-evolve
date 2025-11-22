package com.playground.order.application.service

import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderCancelCommand
import com.playground.order.application.port.inbound.command.OrderCreateCommand
import com.playground.order.application.port.outbound.OrderCommandPort
import com.playground.order.application.port.outbound.OrderEventPort
import com.playground.order.application.port.outbound.OrderQueryPort
import com.playground.order.application.provider.OrderProductProvider
import com.playground.order.application.validator.OrderOwnerValidator
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.contract.domain.vo.ProductInfo
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderProduct
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderCommandService(
    private val orderCommandPort: OrderCommandPort,
    private val orderQueryPort: OrderQueryPort,
    private val orderEventPort: OrderEventPort,
    private val orderProductProvider: OrderProductProvider,
    private val orderOwnerValidator: OrderOwnerValidator,
) : OrderCommandUseCase {
    override fun createOrder(command: OrderCreateCommand): Order {
        val productIds = command.orderProducts.map { it.productId }
        val productInfoMap = orderProductProvider.getVerifiedProductInfos(productIds)

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
                orderId = order.id!!,
                userId = order.userId,
                orderProductDetails,
            )
        orderEventPort.publish(orderCreatedEvent)
    }

    override fun cancelOrder(command: OrderCancelCommand): Order {
        orderOwnerValidator.validate(command.userId, command.orderId)

        val foundOrder = orderQueryPort.findById(command.orderId)
        foundOrder.cancelOrder()
        return orderCommandPort.update(foundOrder)
    }
}
