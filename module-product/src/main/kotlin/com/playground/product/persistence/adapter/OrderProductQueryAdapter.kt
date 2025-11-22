package com.playground.product.persistence.adapter

import com.playground.order.contract.application.OrderProductQueryPort
import com.playground.order.contract.domain.vo.ProductInfo
import com.playground.product.application.port.outbound.ProductQueryPort
import org.springframework.stereotype.Component

@Component
class OrderProductQueryAdapter(
    private val productQueryPort: ProductQueryPort,
) : OrderProductQueryPort {
    override fun getProductInfos(productIds: List<Long>): Map<Long, ProductInfo> {
        val products = productQueryPort.findAllByIds(productIds)

        return products.associateBy(
            keySelector = { it.id!! },
            valueTransform = { ProductInfo(productId = it.id!!, price = it.price, productName = it.name) },
        )
    }
}
