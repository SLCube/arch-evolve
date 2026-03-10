package com.playground.delivery.application.port.inbound

import com.playground.delivery.domain.model.Delivery

interface DeliveryQueryUseCase {
    fun getDeliveryByOrderId(orderId: Long): Delivery
}
