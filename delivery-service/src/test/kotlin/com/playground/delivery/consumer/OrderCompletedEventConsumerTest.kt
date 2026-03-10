package com.playground.delivery.consumer

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.playground.delivery.application.port.inbound.DeliveryUseCase
import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand
import com.playground.delivery.consumer.event.OrderCompletedEvent
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
class OrderCompletedEventConsumerTest {
    private val deliveryUseCase: DeliveryUseCase = mock()
    private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

    private val consumer =
        OrderCompletedEventConsumer(
            deliveryUseCase = deliveryUseCase,
            objectMapper = objectMapper,
        )

    @Test
    fun `레코드를 처리하면 createDelivery가 올바른 필드로 호출되어야 한다`() {
        // given
        val record =
            consumerRecord(
                orderId = 100L,
                userId = 1L,
                receiverName = "홍길동",
                receiverPhoneNumber = "010-1234-5678",
                zipCode = "12345",
                baseAddress = "서울시 강남구 테헤란로 123",
                detailAddress = "101호",
            )

        // when
        consumer.consume(record)

        // then
        val captor = argumentCaptor<DeliveryCreateCommand>()
        verify(deliveryUseCase).createDelivery(captor.capture())

        captor.firstValue.orderId shouldBe 100L
        captor.firstValue.userId shouldBe 1L
        captor.firstValue.receiverName shouldBe "홍길동"
        captor.firstValue.receiverPhoneNumber shouldBe "010-1234-5678"
        captor.firstValue.zipCode shouldBe "12345"
        captor.firstValue.baseAddress shouldBe "서울시 강남구 테헤란로 123"
        captor.firstValue.detailAddress shouldBe "101호"
    }

    @Test
    fun `JSON 역직렬화 실패 시 예외를 rethrow하여 offset 커밋을 방지해야 한다`() {
        // given
        val invalidRecord = ConsumerRecord<String, String>("order-completed", 0, 0L, "1", "invalid-json")

        // when / then
        assertThrows<Exception> {
            consumer.consume(invalidRecord)
        }
    }

    private fun consumerRecord(
        orderId: Long,
        userId: Long,
        receiverName: String,
        receiverPhoneNumber: String,
        zipCode: String,
        baseAddress: String,
        detailAddress: String,
    ): ConsumerRecord<String, String> {
        val event =
            OrderCompletedEvent(
                eventId = UUID.randomUUID(),
                orderId = orderId,
                userId = userId,
                products = emptyList(),
                totalAmount = BigDecimal("50000.00"),
                receiverName = receiverName,
                receiverPhoneNumber = receiverPhoneNumber,
                zipCode = zipCode,
                baseAddress = baseAddress,
                detailAddress = detailAddress,
                occurredAt = LocalDateTime.now(),
            )
        val payload = objectMapper.writeValueAsString(event)
        return ConsumerRecord("order-completed", 0, 0L, orderId.toString(), payload)
    }
}
