package com.playground.order.application.service

import com.playground.order.application.factory.from
import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderCancelCommand
import com.playground.order.application.port.inbound.command.OrderCompleteCommand
import com.playground.order.application.port.inbound.command.OrderCreateCommand
import com.playground.order.application.port.outbound.OrderCommandPort
import com.playground.order.application.port.outbound.OrderEventPort
import com.playground.order.application.port.outbound.OrderQueryPort
import com.playground.order.application.provider.OrderExternalDataProvider
import com.playground.order.contract.domain.event.OrderCompletedEvent
import com.playground.order.contract.domain.event.OrderCreatedEvent
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
    private val orderExternalDataProvider: OrderExternalDataProvider,
) : OrderCommandUseCase {
    override fun createOrder(command: OrderCreateCommand): Order {
        val productIds = command.orderProducts.map { it.productId }
        val productInfoMap = orderExternalDataProvider.getVerifiedProductInfos(productIds)
        val addressInfo = orderExternalDataProvider.getAddressInfoByAddressId(command.userId, command.addressId)
        val orderProducts = command.orderProducts.map { orderProductCommand ->
            val productInfo = productInfoMap.getValue(orderProductCommand.productId)
            OrderProduct(
                productId = orderProductCommand.productId,
                quantity = orderProductCommand.quantity,
                price = productInfo.price,
            )
        }

        val order = Order.createOrder(
            userId = command.userId,
            addressInfo = addressInfo,
            orderProducts = orderProducts,
        )

        val savedOrder = orderCommandPort.save(order)
        orderEventPort.publish(OrderCreatedEvent.from(savedOrder))

        return savedOrder
    }

    override fun completeOrder(command: OrderCompleteCommand): Order {
        val order = orderQueryPort.findById(command.orderId)

        order.completeOrder(command.pgTransactionId)
        val updatedOrder = orderCommandPort.update(order)

        orderEventPort.publish(OrderCompletedEvent.from(updatedOrder))
        return updatedOrder
    }

    override fun cancelOrder(command: OrderCancelCommand): Order {
        val foundOrder = orderQueryPort.findById(command.orderId)
        foundOrder.validateOwner(command.userId)
        foundOrder.cancelOrder()
        return orderCommandPort.update(foundOrder)
    }
}
