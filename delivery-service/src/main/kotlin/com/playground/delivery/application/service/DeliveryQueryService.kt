package com.playground.delivery.application.service

import com.playground.delivery.application.port.inbound.DeliveryQueryUseCase
import com.playground.delivery.application.port.outbound.DeliveryQueryPort
import com.playground.delivery.domain.model.Delivery
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DeliveryQueryService(
    private val deliveryQueryPort: DeliveryQueryPort,
) : DeliveryQueryUseCase {
    override fun getDeliveryByOrderId(orderId: Long): Delivery = deliveryQueryPort.findByOrderIdOrThrow(orderId)
}
