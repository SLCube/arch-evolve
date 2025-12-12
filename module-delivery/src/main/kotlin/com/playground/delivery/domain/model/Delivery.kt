package com.playground.delivery.domain.model

import com.playground.delivery.domain.enum.DeliveryStatus
import java.time.LocalDateTime

class Delivery(
    val id: Long? = null,
    val orderId: Long,
    val userId: Long,
    val deliveryReceiver: DeliveryReceiver,
    val deliveryAddress: DeliveryAddress,
    var deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var shippedAt: LocalDateTime? = null,
    var failedAt: LocalDateTime? = null,
) {
    companion object {
        fun createDelivery(
            orderId: Long,
            userId: Long,
            deliveryReceiver: DeliveryReceiver,
            deliveryAddress: DeliveryAddress,
        ): Delivery {
            return Delivery(
                orderId = orderId,
                userId = userId,
                deliveryReceiver = deliveryReceiver,
                deliveryAddress = deliveryAddress,
            )
        }
    }
}
