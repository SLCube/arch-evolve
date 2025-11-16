package com.playground.order.application.service

import com.playground.order.application.port.`in`.OrderQueryUseCase
import com.playground.order.application.port.out.OrderQueryPort
import com.playground.order.application.provider.OrderProductProvider
import com.playground.order.application.service.result.OrderDetailResult
import com.playground.order.application.validator.OrderOwnerValidator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OrderQueryService(
    private val orderQueryPort: OrderQueryPort,
    private val orderProductProvider: OrderProductProvider,
    private val orderOwnerValidator: OrderOwnerValidator,
) : OrderQueryUseCase {
    override fun getOrder(
        userId: Long,
        orderId: Long,
    ): OrderDetailResult {
        orderOwnerValidator.validate(userId, orderId)

        val order = orderQueryPort.findById(orderId)
        val productIds = order.orderProducts.map { it.productId }
        val productInfoMap = orderProductProvider.getVerifiedProductInfos(productIds)

        return OrderDetailResult.of(order, productInfoMap)
    }
}
