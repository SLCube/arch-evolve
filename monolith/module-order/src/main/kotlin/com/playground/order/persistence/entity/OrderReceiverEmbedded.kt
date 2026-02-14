package com.playground.order.persistence.entity

import com.playground.order.domain.model.OrderReceiver
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class OrderReceiverEmbedded(
    @Column(name = "receiver_name", nullable = false, length = 100)
    val receiverName: String,
    @Column(name = "receiver_phone_number", nullable = false, length = 20)
    val receiverPhoneNumber: String,
) {
    companion object {
        fun toEmbedded(domain: OrderReceiver): OrderReceiverEmbedded {
            return OrderReceiverEmbedded(
                receiverName = domain.receiverName,
                receiverPhoneNumber = domain.receiverPhoneNumber,
            )
        }
    }
}