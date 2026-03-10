package com.playground.delivery.persistence.entity

import com.playground.delivery.domain.model.DeliveryReceiver
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class DeliveryReceiverEmbedded(
    @Column(name = "receiver_name", nullable = false, length = 50)
    val receiverName: String,
    @Column(name = "receiver_phone_number", nullable = false, length = 20)
    val receiverPhoneNumber: String,
) {
    companion object {
        fun from(receiver: DeliveryReceiver): DeliveryReceiverEmbedded =
            DeliveryReceiverEmbedded(
                receiverName = receiver.receiverName,
                receiverPhoneNumber = receiver.receiverPhoneNumber,
            )
    }
}
