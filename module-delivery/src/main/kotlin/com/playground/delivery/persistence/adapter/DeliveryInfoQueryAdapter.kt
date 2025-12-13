package com.playground.delivery.persistence.adapter

import com.playground.delivery.contract.application.outbound.DeliveryInfoQueryPort
import com.playground.delivery.contract.domain.vo.DeliveryInfo
import com.playground.delivery.domain.exception.DeliveryNotFoundException
import com.playground.delivery.persistence.repository.DeliveryRepository
import org.springframework.stereotype.Component

@Component
class DeliveryInfoQueryAdapter(
    private val deliveryRepository: DeliveryRepository,
): DeliveryInfoQueryPort {
    override fun getDeliveryInfoByOrderId(orderId: Long): DeliveryInfo {
        val delivery =
            deliveryRepository.findByOrderId(orderId)
                .orElseThrow { DeliveryNotFoundException(orderId) }

        return DeliveryInfo(
            deliveryId = delivery.id!!,
            orderId = delivery.orderId,
            userId = delivery.userId,
            receiverName = delivery.deliveryReceiver.receiverName,
            receiverPhoneNumber = delivery.deliveryReceiver.receiverPhoneNumber,
            zipCode = delivery.deliveryAddress.zipCode,
            baseAddress = delivery.deliveryAddress.baseAddress,
            detailAddress = delivery.deliveryAddress.detailAddress,
            deliveryStatus = delivery.status.name,
        )
    }
}
