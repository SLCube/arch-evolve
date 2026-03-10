package com.playground.delivery.persistence.repository

import com.playground.delivery.persistence.entity.DeliveryJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface DeliveryRepository : JpaRepository<DeliveryJpaEntity, Long> {
    fun findByOrderId(orderId: Long): Optional<DeliveryJpaEntity>
}
