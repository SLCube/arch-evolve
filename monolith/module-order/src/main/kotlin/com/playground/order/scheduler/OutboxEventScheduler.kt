package com.playground.order.scheduler

import com.playground.order.application.port.inbound.OrderOutboxEventUseCase
import com.playground.common.log.utils.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxEventScheduler(
    private val orderOutboxEventUseCase: OrderOutboxEventUseCase,
) {
    private val log = logger()

    @Scheduled(fixedDelay = 200)
    fun pollAndPublish() {
        try {
            orderOutboxEventUseCase.pollAndPublishEvents()
        } catch (e: Exception) {
            log.error("Outbox 배치 발행 실패", e)
        }
        orderOutboxEventUseCase.recordMetrics()
    }
}
