package com.playground.order.scheduler

import com.playground.order.application.port.inbound.OrderOutboxEventUseCase
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxEventScheduler(
    private val orderOutboxEventUseCase: OrderOutboxEventUseCase,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000)
    fun pollAndPublish() {
        val pendingEvents = orderOutboxEventUseCase.findPendingEvents()

        pendingEvents.forEach { outbox ->
            try {
                orderOutboxEventUseCase.publishEvent(outbox)
            } catch (e: Exception) {
                log.error("Outbox 이벤트 발행 실패 [eventId={}]", outbox.eventId, e)
            }
        }
    }
}
