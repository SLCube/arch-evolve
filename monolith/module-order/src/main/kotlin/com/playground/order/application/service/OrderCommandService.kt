package com.playground.order.application.service

import com.playground.order.application.factory.from
import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderCancelCommand
import com.playground.order.application.port.inbound.command.OrderCompleteCommand
import com.playground.order.application.port.inbound.command.OrderCreateCommand
import com.playground.order.application.port.inbound.command.OrderFailCommand
import com.playground.order.application.port.outbound.OrderCommandPort
import com.playground.order.application.port.outbound.OrderEventPort
import com.playground.order.application.port.outbound.OrderQueryPort
import com.playground.order.application.port.outbound.PaymentPort
import com.playground.order.application.provider.OrderExternalDataProvider
import com.playground.order.contract.domain.event.OrderCompletedEvent
import com.playground.order.contract.domain.event.OrderFailedEvent
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderProduct
import com.playground.product.contract.domain.vo.ProductInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderCommandService(
    private val orderCommandPort: OrderCommandPort,
    private val orderQueryPort: OrderQueryPort,
    private val orderEventPort: OrderEventPort,
    private val orderExternalDataProvider: OrderExternalDataProvider,
    private val paymentPort: PaymentPort,
) : OrderCommandUseCase {
    override fun createOrder(command: OrderCreateCommand): Order {
        val productIds = command.orderProducts.map { it.productId }
        val productInfoMap = orderExternalDataProvider.getVerifiedProductInfos(productIds)
        val addressInfo = orderExternalDataProvider.getAddressInfoByAddressId(command.userId, command.addressId)
        val orderProducts = mapToOrderProducts(command, productInfoMap)

        val order = Order.createOrder(
            userId = command.userId,
            addressInfo = addressInfo,
            orderProducts = orderProducts,
        )

        val savedOrder = orderCommandPort.save(order)
        val pgTransactionId = paymentPort.authorize(command.userId, savedOrder.id!!, savedOrder.totalPrice)
        savedOrder.completeOrder(pgTransactionId)
        return orderCommandPort.update(savedOrder)
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

    override fun failOrder(command: OrderFailCommand): Order {
        val order = orderQueryPort.findById(command.orderId)
        order.fail()
        val updatedOrder = orderCommandPort.update(order)

        orderEventPort.publish(OrderFailedEvent.from(updatedOrder))

        return updatedOrder
    }

    private fun mapToOrderProducts(
        command: OrderCreateCommand,
        productInfoMap: Map<Long, ProductInfo>,
    ): List<OrderProduct> {
        return command.orderProducts.map { orderProductCommand ->
            val productInfo = productInfoMap.getValue(orderProductCommand.productId)
            OrderProduct(
                productId = orderProductCommand.productId,
                quantity = orderProductCommand.quantity,
                price = productInfo.price,
            )
        }
    }
}
