package com.playground.delivery.application.port.outbound

import com.playground.delivery.domain.model.Delivery

interface DeliveryCommandPort {
    fun save(delivery: Delivery): Delivery

    fun update(delivery: Delivery): Delivery
}
