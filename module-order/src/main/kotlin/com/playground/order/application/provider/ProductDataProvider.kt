package com.playground.order.application.provider

import com.playground.order.domain.exception.OrderableProductNotFoundException
import com.playground.product.contract.application.outbound.ProductInfoQueryPort
import com.playground.product.contract.domain.vo.ProductInfo
import org.springframework.stereotype.Component

@Component
class ProductDataProvider(
    private val productInfoQueryPort: ProductInfoQueryPort,
) {
    fun getVerifiedProductInfos(productIds: List<Long>): Map<Long, ProductInfo> {
        val productInfoMap = productInfoQueryPort.getProductInfos(productIds)

        if (productInfoMap.size != productIds.distinct().size) {
            val notFoundProductId = (productIds.toSet() - productInfoMap.keys).first()
            throw OrderableProductNotFoundException(notFoundProductId)
        }

        return productInfoMap
    }
}
