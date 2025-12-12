package com.playground.delivery.application.port.outbound

import com.playground.delivery.domain.model.Delivery
import java.util.Optional

interface DeliveryQueryPort {
    fun findByOrderId(orderId: Long): Optional<Delivery>
}