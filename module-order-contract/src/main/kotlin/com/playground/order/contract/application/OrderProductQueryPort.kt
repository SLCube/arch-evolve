package com.playground.order.contract.application

import com.playground.order.contract.domain.vo.ProductInfo


fun interface OrderProductQueryPort {
    fun getProductInfos(productIds: List<Long>): Map<Long, ProductInfo>
}
