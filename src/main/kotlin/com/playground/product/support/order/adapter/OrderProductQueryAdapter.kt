package com.playground.product.support.order.adapter

import com.playground.order.application.port.out.OrderProductQueryPort
import com.playground.order.domain.vo.ProductInfo
import com.playground.product.application.port.out.ProductQueryPort
import org.springframework.stereotype.Component

@Component
class OrderProductQueryAdapter(
    private val productQueryPort: ProductQueryPort
): OrderProductQueryPort {

    override fun getProductInfos(productIds: List<Long>): Map<Long, ProductInfo> {
        val products = productQueryPort.findAllByIds(productIds)

        return products.associateBy(
            keySelector = { requireNotNull(it.id) },
            valueTransform = { ProductInfo(productId = requireNotNull(it.id), price = it.price) }
        )
    }
}