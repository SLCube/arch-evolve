package com.playground.payment.application.service

import com.playground.payment.application.port.outbound.PaymentCommandPort
import com.playground.payment.application.port.outbound.PaymentEventPort
import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.application.port.outbound.PaymentQueryPort
import com.playground.payment.contract.domain.event.PaymentCompletedEvent
import com.playground.payment.contract.domain.event.PaymentFailedEvent
import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.exception.PaymentFailedException
import com.playground.payment.domain.model.Payment
import com.playground.payment.domain.vo.PgAuthorizationResult
import com.playground.payment.fixture.application.command.PaymentCommandTestFixture
import com.playground.payment.fixture.application.domain.PaymentDomainTestFixture
import com.playground.payment.fixture.application.domain.PaymentMethodDomainTestFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.math.BigDecimal

@Suppress("NonAsciiCharacters")
class PaymentServiceTest {

    private val paymentQueryPort: PaymentQueryPort = mock()
    private val paymentCommandPort: PaymentCommandPort = mock()
    private val paymentGatewayPort: PaymentGatewayPort = mock()
    private val paymentEventPort: PaymentEventPort = mock()
    private val paymentMethodQueryPort: PaymentMethodQueryPort = mock()

    private val paymentService: PaymentService = PaymentService(
        paymentQueryPort = paymentQueryPort,
        paymentCommandPort = paymentCommandPort,
        paymentGatewayPort = paymentGatewayPort,
        paymentEventPort = paymentEventPort,
        paymentMethodQueryPort = paymentMethodQueryPort,
    )

    @Test
    fun `이미 결제 정보가 존재하면 PG 요청 없이 기존 결제를 반환해야 한다`() {
        // given
        val existingPayment = PaymentDomainTestFixture.mockPayment(
            status = PaymentStatus.COMPLETED,
            pgTransactionId = "pg-123",
            approvalNumber = "appr-1",
        )

        val command = PaymentCommandTestFixture.authorizeCommand(
            userId = existingPayment.userId,
            orderId = existingPayment.orderId,
            amount = existingPayment.amount,
        )

        given(paymentQueryPort.findByOrderId(eq(command.orderId)))
            .willReturn(existingPayment)

        // when
        val result = paymentService.authorizePayment(command)

        // then
        result shouldBe existingPayment

        verify(paymentQueryPort).findByOrderId(command.orderId)
        verify(paymentGatewayPort, never()).requestAuthorization(any(), any())
        verify(paymentMethodQueryPort, never()).findDefaultByUserId(any())
        verify(paymentCommandPort, never()).save(any())
        verify(paymentEventPort, never()).publish(any())
    }

    @Test
    fun `결제 승인이 성공하면 Payment가 COMPLETED로 저장되고 이벤트가 발행되어야 한다`() {
        // given
        val command = PaymentCommandTestFixture.authorizeCommand(
            userId = 5L,
            orderId = 200L,
            amount = BigDecimal("55000.00"),
        )

        val paymentMethod = PaymentMethodDomainTestFixture.mockPaymentMethod(
            userId = command.userId,
            billingKey = "billing-key-123",
            isDefault = true,
        )

        val pgTransactionId = "PG-TX-001"
        val approvalNumber = "APR-001"
        val pgResult = PgAuthorizationResult(
            isSuccess = true,
            pgTransactionId = pgTransactionId,
            approvalNumber = approvalNumber,
            failReason = null,
        )

        given(paymentQueryPort.findByOrderId(eq(command.orderId)))
            .willReturn(null)
        given(paymentMethodQueryPort.findDefaultByUserId(eq(command.userId)))
            .willReturn(paymentMethod)
        given(paymentGatewayPort.requestAuthorization(eq(paymentMethod.billingKey), eq(command.amount)))
            .willReturn(pgResult)
        given(paymentCommandPort.save(any()))
            .willAnswer { invocation -> invocation.arguments[0] as Payment }

        // when
        val result = paymentService.authorizePayment(command)

        // then
        result.status shouldBe PaymentStatus.COMPLETED
        result.pgTransactionId shouldBe pgTransactionId
        result.approvalNumber shouldBe approvalNumber

        verify(paymentGatewayPort).requestAuthorization(paymentMethod.billingKey, command.amount)
        verify(paymentMethodQueryPort).findDefaultByUserId(command.userId)

        verify(paymentCommandPort).save(
            check<Payment> { saved ->
                saved.status shouldBe PaymentStatus.COMPLETED
                saved.pgTransactionId shouldBe pgTransactionId
                saved.approvalNumber shouldBe approvalNumber
            }
        )

        verify(paymentEventPort).publish(
            check<PaymentCompletedEvent> {
                it.orderId shouldBe command.orderId
                it.userId shouldBe command.userId
                it.amount shouldBe command.amount
                it.pgTransactionId shouldBe pgTransactionId
            }
        )
    }

    @Test
    fun `PG 승인 실패 시 PaymentFailedException을 던지고 실패 상태로 저장해야 한다`() {
        // given
        val command = PaymentCommandTestFixture.authorizeCommand(
            userId = 7L,
            orderId = 300L,
            amount = BigDecimal("125000.00"),
        )

        val paymentMethod = PaymentMethodDomainTestFixture.mockPaymentMethod(
            userId = command.userId,
            billingKey = "billing-key-fail",
        )

        val failReason = "한도 초과"
        val pgResult = PgAuthorizationResult(
            isSuccess = false,
            pgTransactionId = null,
            approvalNumber = null,
            failReason = failReason,
        )

        given(paymentQueryPort.findByOrderId(eq(command.orderId)))
            .willReturn(null)
        given(paymentMethodQueryPort.findDefaultByUserId(eq(command.userId)))
            .willReturn(paymentMethod)
        given(paymentGatewayPort.requestAuthorization(eq(paymentMethod.billingKey), eq(command.amount)))
            .willReturn(pgResult)
        given(paymentCommandPort.save(any()))
            .willAnswer { invocation -> invocation.arguments[0] as Payment }

        // when & then
        shouldThrow<PaymentFailedException> {
            paymentService.authorizePayment(command)
        }.message shouldContain failReason

        verify(paymentCommandPort).save(
            check<Payment> { failedPayment ->
                failedPayment.status shouldBe PaymentStatus.FAILED
                failedPayment.failReason shouldBe failReason
            }
        )

        verify(paymentEventPort).publish(
            check<PaymentFailedEvent> { event ->
                event.orderId shouldBe command.orderId
                event.userId shouldBe command.userId
                event.failReason shouldBe failReason
            }
        )
    }
}
