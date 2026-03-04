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

    @Scheduled(fixedDelay = 1000)
    fun pollAndPublish() {
        val pendingEvents = orderOutboxEventUseCase.findPendingEvents()
        if (pendingEvents.isEmpty()) return

        try {
            orderOutboxEventUseCase.publishEvents(pendingEvents)
        } catch (e: Exception) {
            log.error("Outbox 배치 발행 실패", e)
        }
    }
}
