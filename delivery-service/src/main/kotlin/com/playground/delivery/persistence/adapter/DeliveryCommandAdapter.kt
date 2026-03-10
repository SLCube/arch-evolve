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
) : DeliveryCommandPort {
    override fun save(delivery: Delivery): Delivery {
        val entity = DeliveryJpaEntity.toJpaEntity(delivery)
        return deliveryRepository.save(entity).toDomain()
    }

    override fun update(delivery: Delivery): Delivery {
        val entity =
            deliveryRepository
                .findByOrderId(delivery.orderId)
                .orElseThrow { DeliveryNotFoundException(delivery.orderId) }

        entity.updateStatus(
            status = delivery.deliveryStatus,
            shippedAt = delivery.shippedAt,
            deliveredAt = delivery.deliveredAt,
            failedAt = delivery.failedAt,
        )

        return entity.toDomain()
    }
}
