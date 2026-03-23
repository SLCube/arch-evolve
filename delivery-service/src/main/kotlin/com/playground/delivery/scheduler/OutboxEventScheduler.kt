package com.playground.delivery.scheduler

import com.playground.delivery.application.port.inbound.DeliveryOutboxEventUseCase
import com.playground.delivery.common.log.utils.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxEventScheduler(
    private val deliveryOutboxEventUseCase: DeliveryOutboxEventUseCase,
) {
    private val log = logger()

    @Scheduled(fixedDelay = 200)
    fun pollAndPublish() {
        try {
            deliveryOutboxEventUseCase.pollAndPublishEvents()
        } catch (e: Exception) {
            log.error("Outbox 배치 발행 실패", e)
        }
        deliveryOutboxEventUseCase.recordMetrics()
    }
}
