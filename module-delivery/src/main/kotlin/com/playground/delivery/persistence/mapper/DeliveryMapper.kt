package com.playground.delivery.persistence.mapper

import com.playground.delivery.domain.model.Delivery
import com.playground.delivery.domain.model.DeliveryAddress
import com.playground.delivery.domain.model.DeliveryReceiver
import com.playground.delivery.persistence.entity.DeliveryAddressEmbedded
import com.playground.delivery.persistence.entity.DeliveryJpaEntity
import com.playground.delivery.persistence.entity.DeliveryReceiverEmbedded

fun DeliveryJpaEntity.toDomain(): Delivery =
    Delivery(
        id = id,
        orderId = orderId,
        userId = userId,
        deliveryReceiver = deliveryReceiver.toDomain(),
        deliveryAddress = deliveryAddress.toDomain(),
        deliveryStatus = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        shippedAt = shippedAt,
        failedAt = failedAt,
    )

fun DeliveryAddressEmbedded.toDomain(): DeliveryAddress =
    DeliveryAddress(
        zipCode = zipCode,
        baseAddress = baseAddress,
        detailAddress = detailAddress,
    )

fun DeliveryReceiverEmbedded.toDomain(): DeliveryReceiver =
    DeliveryReceiver(
        receiverName = receiverName,
        receiverPhoneNumber = receiverPhoneNumber,
    )
