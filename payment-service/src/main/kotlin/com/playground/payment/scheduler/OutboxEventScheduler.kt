package com.playground.payment.scheduler

import com.playground.payment.application.port.inbound.PaymentOutboxEventUseCase
import com.playground.payment.common.log.utils.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxEventScheduler(
    private val paymentOutboxEventUseCase: PaymentOutboxEventUseCase,
) {
    private val log = logger()

    @Scheduled(fixedDelay = 1000)
    fun pollAndPublish() {
        val pendingEvents = paymentOutboxEventUseCase.findPendingEvents()
        if (pendingEvents.isEmpty()) return

        try {
            paymentOutboxEventUseCase.publishEvents(pendingEvents)
        } catch (e: Exception) {
            log.error("Outbox 배치 발행 실패", e)
        }
    }
}
