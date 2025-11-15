package com.playground.order.application.port.out

import com.playground.order.domain.vo.ProductInfo

fun interface OrderProductQueryPort {
    fun getProductInfos(productIds: List<Long>): Map<Long, ProductInfo>
}