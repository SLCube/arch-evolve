package com.playground.delivery.persistence.adapter

import com.playground.delivery.application.port.outbound.DeliveryQueryPort
import com.playground.delivery.domain.model.Delivery
import com.playground.delivery.persistence.mapper.toDomain
import com.playground.delivery.persistence.repository.DeliveryRepository
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class DeliveryQueryAdapter(
    private val deliveryRepository: DeliveryRepository,
): DeliveryQueryPort {
    override fun findByOrderId(orderId: Long): Optional<Delivery> {
        return deliveryRepository.findByOrderId(orderId)
            .map { it.toDomain() }
    }
}
