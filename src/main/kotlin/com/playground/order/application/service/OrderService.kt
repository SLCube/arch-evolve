package com.playground.order.application.service

import com.playground.order.application.port.`in`.OrderUseCase
import com.playground.order.application.port.`in`.command.OrderCreateCommand
import com.playground.order.application.port.out.OrderCommandPort
import com.playground.order.application.port.out.OrderQueryPort
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderProduct
import com.playground.product.application.port.`in`.ProductUseCase
import com.playground.product.application.port.`in`.command.DecreaseStockCommand
import com.playground.product.application.port.`in`.query.GetProductQuery
import com.playground.user.application.port.out.UserQueryPort
import com.playground.user.domain.exception.UserNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    private val orderCommandPort: OrderCommandPort,
    private val orderQueryPort: OrderQueryPort,
    private val userQueryPort: UserQueryPort,
    private val productUseCase: ProductUseCase
): OrderUseCase {

    override fun createOrder(command: OrderCreateCommand): Order {
        userQueryPort.findById(command.userId)
            .orElseThrow { UserNotFoundException() }

        val order = Order(
            userId = command.userId,
            totalPrice = 0
        )

        command.orderProducts.forEach { orderProductCommand ->
            val productId = orderProductCommand.productId
            val quantity = orderProductCommand.quantity

            val product = productUseCase.getProduct(GetProductQuery(productId))
            productUseCase.decreaseStock(DecreaseStockCommand(productId, quantity))

            val orderProduct = OrderProduct(
                productId = productId,
                quantity = quantity,
                price = product.price
            )

            order.addOrderProduct(orderProduct)
        }

        order.calculateTotalPrice()

        return orderCommandPort.save(order)
    }
}