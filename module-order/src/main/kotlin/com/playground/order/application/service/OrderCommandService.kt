package com.playground.order.application.service

import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderCancelCommand
import com.playground.order.application.port.inbound.command.OrderCompleteCommand
import com.playground.order.application.port.inbound.command.OrderCreateCommand
import com.playground.order.application.port.outbound.OrderCommandPort
import com.playground.order.application.port.outbound.OrderEventPort
import com.playground.order.application.port.outbound.OrderQueryPort
import com.playground.order.application.provider.OrderExternalDataProvider
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderAddress
import com.playground.order.domain.model.OrderProduct
import com.playground.order.domain.model.OrderReceiver
import com.playground.product.contract.domain.vo.ProductInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

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

        val order = createOrderAggregate(command, productInfoMap)

        val savedOrder = orderCommandPort.save(order)

        publishOrderCreationEvent(savedOrder)

        return savedOrder
    }

    override fun completeOrder(command: OrderCompleteCommand): Order {
        val order = orderQueryPort.findById(command.orderId)
        order.completeOrder(command.pgTransactionId)
        return orderCommandPort.update(order)
    }

    private fun createOrderAggregate(
        command: OrderCreateCommand,
        productInfoMap: Map<Long, ProductInfo>,
    ): Order {
        val addressInfo = orderExternalDataProvider.getAddressInfoByAddressId(command.userId, command.addressId)
        val order =
            Order(
                userId = command.userId,
                totalPrice = BigDecimal.ZERO,
                orderAddress = OrderAddress.fromAddressInfo(addressInfo),
                orderReceiver = OrderReceiver.fromAddressInfo(addressInfo)
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
                order.totalPrice,
            )
        orderEventPort.publish(orderCreatedEvent)
    }

    override fun cancelOrder(command: OrderCancelCommand): Order {
        val foundOrder = orderQueryPort.findById(command.orderId)
        foundOrder.validateOwner(command.userId)
        foundOrder.cancelOrder()
        return orderCommandPort.update(foundOrder)
    }
}
