package com.playground.delivery.domain.model

import com.playground.delivery.domain.enum.DeliveryStatus
import com.playground.delivery.domain.exception.DeliveryStatusInvalidException
import java.time.LocalDateTime

class Delivery(
    val id: Long? = null,
    val orderId: Long,
    val userId: Long,
    val deliveryReceiver: DeliveryReceiver,
    val deliveryAddress: DeliveryAddress,
    var deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime? = null,
    var shippedAt: LocalDateTime? = null,
    var deliveredAt: LocalDateTime? = null,
    var failedAt: LocalDateTime? = null,
) {
    fun startDelivery() {
        if (deliveryStatus != DeliveryStatus.PENDING) {
            throw DeliveryStatusInvalidException(
                deliveryId = id,
                currentStatus = deliveryStatus,
                targetStatus = DeliveryStatus.SHIPPING,
            )
        }
        val now = LocalDateTime.now()
        deliveryStatus = DeliveryStatus.SHIPPING
        shippedAt = now
        updatedAt = now
    }

    fun completeDelivery() {
        if (deliveryStatus != DeliveryStatus.SHIPPING) {
            throw DeliveryStatusInvalidException(
                deliveryId = id,
                currentStatus = deliveryStatus,
                targetStatus = DeliveryStatus.DELIVERED,
            )
        }
        val now = LocalDateTime.now()
        deliveryStatus = DeliveryStatus.DELIVERED
        updatedAt = now
        deliveredAt = now
    }

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
