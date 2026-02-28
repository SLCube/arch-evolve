package com.playground.payment.application.support

import com.playground.payment.application.port.outbound.OutboxCommandPort
import com.playground.payment.application.port.outbound.PaymentCommandPort
import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.application.port.outbound.PaymentQueryPort
import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.event.PaymentAuthorizedEvent
import com.playground.payment.domain.event.PaymentDomainEvent
import com.playground.payment.domain.event.PaymentFailedEvent
import com.playground.payment.domain.model.Payment
import com.playground.payment.domain.outbox.OutboxEventType
import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.domain.outbox.PaymentEventOutbox
import com.playground.payment.domain.vo.PgAuthorizationResult
import com.playground.payment.fixture.application.command.PaymentCommandTestFixture
import com.playground.payment.fixture.application.domain.PaymentDomainTestFixture
import com.playground.payment.fixture.application.domain.PaymentMethodDomainTestFixture
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.LocalDateTime
import java.util.UUID

@Suppress("NonAsciiCharacters")
class PaymentTransactionManagerTest {
    private val paymentQueryPort: PaymentQueryPort = mock()
    private val paymentCommandPort: PaymentCommandPort = mock()
    private val paymentMethodQueryPort: PaymentMethodQueryPort = mock()
    private val outboxCommandPort: OutboxCommandPort = mock()
    private val outboxFactory: OutboxFactory = mock()

    private val paymentTransactionManager =
        PaymentTransactionManager(
            paymentQueryPort = paymentQueryPort,
            paymentCommandPort = paymentCommandPort,
            paymentMethodQueryPort = paymentMethodQueryPort,
            outboxCommandPort = outboxCommandPort,
            outboxFactory = outboxFactory,
        )

    @Test
    fun `존재하는 orderId로 조회하면 Payment를 반환해야 한다`() {
        // given
        val orderId = 100L
        val payment = PaymentDomainTestFixture.mockPayment(orderId = orderId)
        given(paymentQueryPort.findByOrderId(eq(orderId))).willReturn(payment)

        // when
        val result = paymentTransactionManager.findByOrderId(orderId)

        // then
        result shouldBe payment
        verify(paymentQueryPort).findByOrderId(orderId)
    }

    @Test
    fun `존재하지 않는 orderId로 조회하면 null을 반환해야 한다`() {
        // given
        val orderId = 999L
        given(paymentQueryPort.findByOrderId(eq(orderId))).willReturn(null)

        // when
        val result = paymentTransactionManager.findByOrderId(orderId)

        // then
        result shouldBe null
        verify(paymentQueryPort).findByOrderId(orderId)
    }

    @Test
    fun `userId로 조회하면 해당 사용자의 PaymentMethod를 반환해야 한다`() {
        // given
        val userId = 2L
        val paymentMethod = PaymentMethodDomainTestFixture.mockPaymentMethod(userId = userId)
        given(paymentMethodQueryPort.findDefaultByUserId(eq(userId))).willReturn(paymentMethod)

        // when
        val result = paymentTransactionManager.getPaymentMethod(userId)

        // then
        result shouldBe paymentMethod
        verify(paymentMethodQueryPort).findDefaultByUserId(userId)
    }

    @Test
    fun `PG 성공 시 COMPLETED 상태 Payment를 저장하고 Outbox 이벤트를 발행해야 한다`() {
        // given
        val command = PaymentCommandTestFixture.authorizeCommand(userId = 5L, orderId = 200L)
        val billingKey = "billing-key-success"
        val pgResult =
            PgAuthorizationResult(
                isSuccess = true,
                pgTransactionId = "PG-TX-001",
                approvalNumber = "APR-001",
                failReason = null,
            )

        val completedPayment =
            PaymentDomainTestFixture.mockPayment(
                userId = command.userId,
                orderId = command.orderId,
                status = PaymentStatus.COMPLETED,
                pgTransactionId = "PG-TX-001",
                approvalNumber = "APR-001",
            )

        val savedOutbox =
            PaymentEventOutbox(
                id = 1L,
                eventId = UUID.randomUUID(),
                eventType = OutboxEventType.PAYMENT_AUTHORIZED,
                payload = "{}",
                status = OutboxStatus.PENDING,
                occurredAt = LocalDateTime.now(),
            )

        given(paymentCommandPort.save(any())).willReturn(completedPayment)
        given(outboxFactory.from(any())).willReturn(savedOutbox)
        given(outboxCommandPort.save(any())).willReturn(savedOutbox)

        // when
        val result = paymentTransactionManager.savePaymentResult(command, pgResult, billingKey)

        // then
        result.status shouldBe PaymentStatus.COMPLETED

        val paymentCaptor = argumentCaptor<Payment>()
        verify(paymentCommandPort).save(paymentCaptor.capture())
        with(paymentCaptor.firstValue) {
            status shouldBe PaymentStatus.COMPLETED
            pgTransactionId shouldBe "PG-TX-001"
            approvalNumber shouldBe "APR-001"
            approveAt shouldNotBe null
        }

        val domainEventCaptor = argumentCaptor<PaymentDomainEvent>()
        verify(outboxFactory).from(domainEventCaptor.capture())
        domainEventCaptor.firstValue.shouldBeInstanceOf<PaymentAuthorizedEvent>()
        with(domainEventCaptor.firstValue as PaymentAuthorizedEvent) {
            orderId shouldBe command.orderId
            userId shouldBe command.userId
            pgTransactionId shouldBe "PG-TX-001"
        }

        verify(outboxCommandPort).save(eq(savedOutbox))
    }

    @Test
    fun `PG 실패 시 FAILED 상태 Payment를 저장하고 Outbox 이벤트를 발행해야 한다`() {
        // given
        val command = PaymentCommandTestFixture.authorizeCommand(userId = 7L, orderId = 300L)
        val billingKey = "billing-key-fail"
        val failReason = "한도 초과"
        val pgResult =
            PgAuthorizationResult(
                isSuccess = false,
                pgTransactionId = null,
                approvalNumber = null,
                failReason = failReason,
            )

        val failedPayment =
            PaymentDomainTestFixture.mockPayment(
                userId = command.userId,
                orderId = command.orderId,
                status = PaymentStatus.FAILED,
                failReason = failReason,
            )

        val savedOutbox =
            PaymentEventOutbox(
                id = 2L,
                eventId = UUID.randomUUID(),
                eventType = OutboxEventType.PAYMENT_FAILED,
                payload = "{}",
                status = OutboxStatus.PENDING,
                occurredAt = LocalDateTime.now(),
            )

        given(paymentCommandPort.save(any())).willReturn(failedPayment)
        given(outboxFactory.from(any())).willReturn(savedOutbox)
        given(outboxCommandPort.save(any())).willReturn(savedOutbox)

        // when
        val result = paymentTransactionManager.savePaymentResult(command, pgResult, billingKey)

        // then
        result.status shouldBe PaymentStatus.FAILED

        val paymentCaptor = argumentCaptor<Payment>()
        verify(paymentCommandPort).save(paymentCaptor.capture())
        with(paymentCaptor.firstValue) {
            status shouldBe PaymentStatus.FAILED
            this.failReason shouldBe failReason
        }

        val domainEventCaptor = argumentCaptor<PaymentDomainEvent>()
        verify(outboxFactory).from(domainEventCaptor.capture())
        domainEventCaptor.firstValue.shouldBeInstanceOf<PaymentFailedEvent>()
        with(domainEventCaptor.firstValue as PaymentFailedEvent) {
            orderId shouldBe command.orderId
            userId shouldBe command.userId
            this.failReason shouldBe failReason
        }

        verify(outboxCommandPort).save(eq(savedOutbox))
    }

    @Test
    fun `PG 실패 시 failReason이 null이면 기본 메시지로 대체되어 저장되어야 한다`() {
        // given
        val command = PaymentCommandTestFixture.authorizeCommand(userId = 7L, orderId = 400L)
        val billingKey = "billing-key-fail-null"
        val pgResult =
            PgAuthorizationResult(
                isSuccess = false,
                pgTransactionId = null,
                approvalNumber = null,
                failReason = null,
            )

        val savedOutbox =
            PaymentEventOutbox(
                id = 3L,
                eventId = UUID.randomUUID(),
                eventType = OutboxEventType.PAYMENT_FAILED,
                payload = "{}",
                status = OutboxStatus.PENDING,
                occurredAt = LocalDateTime.now(),
            )

        given(paymentCommandPort.save(any())).willReturn(
            PaymentDomainTestFixture.mockPayment(
                status = PaymentStatus.FAILED,
                failReason = "PG사로부터 상세 오류 정보가 수신되지 않았습니다.",
            ),
        )
        given(outboxFactory.from(any())).willReturn(savedOutbox)
        given(outboxCommandPort.save(any())).willReturn(savedOutbox)

        // when
        paymentTransactionManager.savePaymentResult(command, pgResult, billingKey)

        // then
        val paymentCaptor = argumentCaptor<Payment>()
        verify(paymentCommandPort).save(paymentCaptor.capture())
        with(paymentCaptor.firstValue) {
            status shouldBe PaymentStatus.FAILED
            failReason shouldBe "PG사로부터 상세 오류 정보가 수신되지 않았습니다."
        }

        val domainEventCaptor = argumentCaptor<PaymentDomainEvent>()
        verify(outboxFactory).from(domainEventCaptor.capture())
        with(domainEventCaptor.firstValue as PaymentFailedEvent) {
            failReason shouldBe "PG사로부터 상세 오류 정보가 수신되지 않았습니다."
        }
    }
}
