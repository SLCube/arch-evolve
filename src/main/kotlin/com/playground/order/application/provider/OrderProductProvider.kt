package com.playground.order.application.provider

import com.playground.order.application.port.`in`.command.OrderCreateCommand
import com.playground.order.application.port.out.OrderProductQueryPort
import com.playground.order.domain.exception.OrderableProductNotFoundException
import com.playground.order.domain.vo.ProductInfo
import org.springframework.stereotype.Component

@Component
class OrderProductProvider(
    private val orderProductQueryPort: OrderProductQueryPort,
) {
    fun getVerifiedProductInfos(command: OrderCreateCommand): Map<Long, ProductInfo> {
        val productIds = command.orderProducts.map { it.productId }
        val productInfoMap = orderProductQueryPort.getProductInfos(productIds)

        if (productInfoMap.size != productIds.distinct().size) {
            val notFoundProductId = (productIds.toSet() - productInfoMap.keys).first()
            throw OrderableProductNotFoundException(notFoundProductId)
        }

        return productInfoMap
    }
}
