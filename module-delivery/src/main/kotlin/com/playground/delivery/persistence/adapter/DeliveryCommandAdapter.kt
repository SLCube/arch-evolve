package com.playground.delivery.persistence.adapter

import com.playground.delivery.application.port.outbound.DeliveryCommandPort
import com.playground.delivery.domain.exception.DeliveryNotFoundException
import com.playground.delivery.domain.model.Delivery
import com.playground.delivery.persistence.entity.DeliveryJpaEntity
import com.playground.delivery.persistence.mapper.toDomain
import com.playground.delivery.persistence.repository.DeliveryRepository
import org.springframework.stereotype.Component

@Component
class DeliveryCommandAdapter(
    private val deliveryRepository: DeliveryRepository,
): DeliveryCommandPort {
    override fun save(delivery: Delivery): Delivery {
        val deliveryJpaEntity = DeliveryJpaEntity.toJpaEntity(delivery)
        val savedEntity = deliveryRepository.save(deliveryJpaEntity)
        return savedEntity.toDomain()
    }

    override fun update(delivery: Delivery): Delivery {
        val deliveryJpaEntity = deliveryRepository.findByOrderId(delivery.orderId)
                .orElseThrow { DeliveryNotFoundException(delivery.orderId) }

        deliveryJpaEntity.updateStatus(
            status = delivery.deliveryStatus,
            shippedAt = delivery.shippedAt,
            deliveredAt = delivery.deliveredAt,
            failedAt = delivery.failedAt,
        )

        return deliveryJpaEntity.toDomain()
    }
}
