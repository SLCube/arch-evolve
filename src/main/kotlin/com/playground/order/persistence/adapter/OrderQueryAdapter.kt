package com.playground.order.persistence.adapter

import com.playground.order.application.port.out.OrderQueryPort
import com.playground.order.domain.model.Order
import org.springframework.stereotype.Component

@Component
class OrderQueryAdapter(

): OrderQueryPort {
    override fun findById(orderId: Long): Order {
        TODO("Not yet implemented")
    }
}