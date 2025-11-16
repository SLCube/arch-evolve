package com.playground.order.application.port.outbound

import com.playground.order.domain.vo.ProductInfo

fun interface OrderProductQueryPort {
    fun getProductInfos(productIds: List<Long>): Map<Long, ProductInfo>
}
