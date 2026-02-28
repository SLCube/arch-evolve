package com.playground.payment.scheduler

import com.playground.payment.application.port.inbound.PaymentOutboxEventUseCase
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxEventScheduler(
    private val paymentOutboxEventUseCase: PaymentOutboxEventUseCase,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000)
    fun pollAndPublish() {
        val pendingEvents = paymentOutboxEventUseCase.findPendingEvents()

        pendingEvents.forEach { outbox ->
            try {
                paymentOutboxEventUseCase.publishEvent(outbox)
            } catch (e: Exception) {
                log.error("Outbox 이벤트 발행 실패 [eventId={}]", outbox.eventId, e)
            }
        }
    }
}
