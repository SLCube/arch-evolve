package com.playground.payment.application.service

import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.application.support.PaymentTransactionManager
import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.vo.PgAuthorizationResult
import com.playground.payment.fixture.application.command.PaymentCommandTestFixture
import com.playground.payment.fixture.application.domain.PaymentDomainTestFixture
import com.playground.payment.fixture.application.domain.PaymentMethodDomainTestFixture
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.math.BigDecimal

@Suppress("NonAsciiCharacters")
class PaymentServiceTest {
    private val paymentTransactionManager: PaymentTransactionManager = mock()
    private val paymentGatewayPort: PaymentGatewayPort = mock()

    private val paymentService: PaymentService =
        PaymentService(
            paymentTransactionManager = paymentTransactionManager,
            paymentGatewayPort = paymentGatewayPort,
        )

    @Test
    fun `이미 결제 정보가 존재하면 PG 요청 없이 기존 결제를 반환해야 한다`() {
        // given
        val existingPayment =
            PaymentDomainTestFixture.mockPayment(
                status = PaymentStatus.COMPLETED,
                pgTransactionId = "pg-123",
                approvalNumber = "appr-1",
            )

        val command =
            PaymentCommandTestFixture.authorizeCommand(
                userId = existingPayment.userId,
                orderId = existingPayment.orderId,
                amount = existingPayment.amount,
            )

        given(paymentTransactionManager.findByOrderId(eq(command.orderId)))
            .willReturn(existingPayment)

        // when
        val result = paymentService.authorizePayment(command)

        // then
        result shouldBe existingPayment

        verify(paymentTransactionManager).findByOrderId(command.orderId)
        verify(paymentGatewayPort, never()).requestAuthorization(any(), any())
        verify(paymentTransactionManager, never()).savePaymentResult(any(), any(), any())
    }

    @Test
    fun `결제 승인이 성공하면 Payment가 COMPLETED로 저장되어야 한다`() {
        // given
        val command =
            PaymentCommandTestFixture.authorizeCommand(
                userId = 5L,
                orderId = 200L,
                amount = BigDecimal("55000.00"),
            )

        val paymentMethod =
            PaymentMethodDomainTestFixture.mockPaymentMethod(
                userId = command.userId,
                billingKey = "billing-key-123",
                isDefault = true,
            )

        val pgTransactionId = "PG-TX-001"
        val approvalNumber = "APR-001"
        val pgResult =
            PgAuthorizationResult(
                isSuccess = true,
                pgTransactionId = pgTransactionId,
                approvalNumber = approvalNumber,
                failReason = null,
            )

        val completedPayment =
            PaymentDomainTestFixture.mockPayment(
                status = PaymentStatus.COMPLETED,
                pgTransactionId = pgTransactionId,
                approvalNumber = approvalNumber,
            )

        given(paymentTransactionManager.findByOrderId(eq(command.orderId)))
            .willReturn(null)
        given(paymentTransactionManager.getPaymentMethod(eq(command.userId)))
            .willReturn(paymentMethod)
        given(paymentGatewayPort.requestAuthorization(eq(paymentMethod.billingKey), eq(command.amount)))
            .willReturn(pgResult)
        given(paymentTransactionManager.savePaymentResult(any(), any(), any()))
            .willReturn(completedPayment)

        // when
        val result = paymentService.authorizePayment(command)

        // then
        result.status shouldBe PaymentStatus.COMPLETED
        result.pgTransactionId shouldBe pgTransactionId
        result.approvalNumber shouldBe approvalNumber

        verify(paymentGatewayPort).requestAuthorization(paymentMethod.billingKey, command.amount)
        verify(paymentTransactionManager).savePaymentResult(any(), any(), eq(paymentMethod.billingKey))
    }

    @Test
    fun `PG 승인 실패 시 FAILED 상태 Payment를 반환해야 한다`() {
        // given
        val command =
            PaymentCommandTestFixture.authorizeCommand(
                userId = 7L,
                orderId = 300L,
                amount = BigDecimal("125000.00"),
            )

        val paymentMethod =
            PaymentMethodDomainTestFixture.mockPaymentMethod(
                userId = command.userId,
                billingKey = "billing-key-fail",
            )

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
                status = PaymentStatus.FAILED,
                failReason = failReason,
            )

        given(paymentTransactionManager.findByOrderId(eq(command.orderId)))
            .willReturn(null)
        given(paymentTransactionManager.getPaymentMethod(eq(command.userId)))
            .willReturn(paymentMethod)
        given(paymentGatewayPort.requestAuthorization(eq(paymentMethod.billingKey), eq(command.amount)))
            .willReturn(pgResult)
        given(paymentTransactionManager.savePaymentResult(any(), any(), any()))
            .willReturn(failedPayment)

        // when
        val result = paymentService.authorizePayment(command)

        // then
        result.status shouldBe PaymentStatus.FAILED
        result.failReason shouldBe failReason

        verify(paymentGatewayPort).requestAuthorization(paymentMethod.billingKey, command.amount)
        verify(paymentTransactionManager).savePaymentResult(any(), any(), eq(paymentMethod.billingKey))
    }
}
