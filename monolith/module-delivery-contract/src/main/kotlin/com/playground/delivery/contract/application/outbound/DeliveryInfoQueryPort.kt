package com.playground.delivery.contract.application.outbound

import com.playground.delivery.contract.domain.vo.DeliveryInfo

interface DeliveryInfoQueryPort {
    fun getDeliveryInfoByOrderId(orderId: Long): DeliveryInfo?
}