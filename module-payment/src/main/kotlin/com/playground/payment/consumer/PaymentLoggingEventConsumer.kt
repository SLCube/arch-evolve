package com.playground.payment.consumer

import com.playground.common.log.utils.logger
import com.playground.payment.contract.domain.event.PaymentCompletedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentLoggingEventConsumer {
    private val log = logger()

    @TransactionalEventListener
    fun handlePaymentCompletedEvent(event: PaymentCompletedEvent) {
        log.info(
            "New payment created. orderId={}, userId={}, amount={}, pgTransactionId={}",
            event.orderId,
            event.userId,
            event.amount,
            event.pgTransactionId,
        )
    }
}