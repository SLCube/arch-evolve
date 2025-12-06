package com.playground.order.application.provider

import com.playground.order.domain.exception.OrderableProductNotFoundException
import com.playground.product.contract.application.outbound.ProductInfoQueryPort
import com.playground.product.contract.domain.vo.ProductInfo
import com.playground.user.contract.application.port.outbound.AddressInfoQueryPort
import com.playground.user.contract.domain.vo.ReceiverAddressInfo
import org.springframework.stereotype.Component

/**
 * 주문의 생성 및 조회 시 필요한 외부 시스템의 데이터를 통합적으로 조회하는 Provider입니다.
 */
@Component
class OrderExternalDataProvider(
    private val productInfoQueryPort: ProductInfoQueryPort,
    private val addressInfoQueryPort: AddressInfoQueryPort,
) {

    fun getVerifiedProductInfos(productIds: List<Long>): Map<Long, ProductInfo> {
        val productInfoMap = productInfoQueryPort.getProductInfos(productIds)

        if (productInfoMap.size != productIds.distinct().size) {
            val notFoundProductId = (productIds.toSet() - productInfoMap.keys).first()
            throw OrderableProductNotFoundException(notFoundProductId)
        }

        return productInfoMap
    }

    fun getAddressInfoByAddressId(userId: Long, addressId: Long): ReceiverAddressInfo {
        return addressInfoQueryPort.getAddressInfoByAddressId(userId, addressId)
    }
}