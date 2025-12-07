package com.playground.product.persistence.adapter

import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.contract.application.outbound.ProductInfoQueryPort
import com.playground.product.contract.domain.vo.ProductInfo
import org.springframework.stereotype.Component

@Component
class ProductInfoQueryAdapter(
    private val productQueryPort: ProductQueryPort,
) : ProductInfoQueryPort {
    override fun getProductInfos(productIds: List<Long>): Map<Long, ProductInfo> {
        if (productIds.isEmpty()) {
            return emptyMap()
        }

        val products = productQueryPort.findAllByIds(productIds)

        return products.associateBy(
            keySelector = { it.id!! },
            valueTransform = { ProductInfo(productId = it.id!!, price = it.price, productName = it.name) },
        )
    }
}
