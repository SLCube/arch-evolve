package com.playground.payment.consumer

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import com.playground.payment.consumer.event.OrderCreatedEvent
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
class OrderCreatedEventConsumerTest {
    private val paymentUseCase: PaymentUseCase = mock()
    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    private val consumer =
        OrderCreatedEventConsumer(
            paymentUseCase = paymentUseCase,
            objectMapper = objectMapper,
        )

    @Test
    fun `레코드를 처리하면 authorizePayment가 호출되어야 한다`() {
        val record = consumerRecord(orderId = 100L, userId = 1L, amount = "10000.00")

        consumer.consume(record)

        val captor = argumentCaptor<PaymentAuthorizeCommand>()
        verify(paymentUseCase).authorizePayment(captor.capture())

        captor.firstValue.orderId shouldBe 100L
        captor.firstValue.userId shouldBe 1L
        captor.firstValue.amount shouldBe BigDecimal("10000.00")
    }

    private fun consumerRecord(
        orderId: Long,
        userId: Long,
        amount: String,
    ): ConsumerRecord<String, String> {
        val event =
            OrderCreatedEvent(
                eventId = UUID.randomUUID(),
                orderId = orderId,
                userId = userId,
                products = emptyList(),
                totalAmount = BigDecimal(amount),
                occurredAt = LocalDateTime.now(),
            )
        val payload = objectMapper.writeValueAsString(event)
        return ConsumerRecord<String, String>("order-created", 0, 0L, orderId.toString(), payload)
    }
}
