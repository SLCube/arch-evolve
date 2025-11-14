package com.playground.order.application.port.out

import com.playground.order.domain.vo.ProductInfo

interface OrderProductQueryPort {
    fun getProductInfos(productIds: List<Long>): Map<Long, ProductInfo>
}