package com.playground.payment.service

import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.AuthorizePaymentCommand
import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.exception.PaymentFailedException
import com.playground.payment.persistence.entity.PaymentMethodJpaEntity
import com.playground.payment.persistence.repository.PaymentMethodRepository
import com.playground.payment.persistence.repository.PaymentRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal

@Suppress("NonAsciiCharacters")
@SpringBootTest
class PaymentServiceTest(
    @param:Autowired private val paymentUseCase: PaymentUseCase,
    @param:Autowired private val paymentRepository: PaymentRepository,
    @param:Autowired private val paymentMethodRepository: PaymentMethodRepository,
) {
    @Test
    fun `결제 승인 - 성공, Payment 레코드에 COMPLETED 상태와 PG 거래 ID가 기록된다`() {
        val userId = 3L
        val orderId = 102L
        val amount = BigDecimal("50000.00")
        createPaymentMethodFixture(userId, "SUCCESS_KEY") // Mock PG가 성공하도록 설정

        val command = AuthorizePaymentCommand(
            userId = userId,
            orderId = orderId,
            amount = amount,
        )

        paymentUseCase.authorizePayment(command)

        val successRecord = paymentRepository.findByOrderId(orderId).orElse(null)

        successRecord.shouldNotBeNull()

        successRecord.status shouldBe PaymentStatus.COMPLETED

        successRecord.pgTransactionId.shouldNotBeNull()
        successRecord.amount shouldBe amount
    }

    @Test
    fun `결제 승인 - 실패, 결제 한도 초과 시 실패 기록이 남고 예외가 던져진다`() {
        val userId = 2L
        val orderId = 101L
        val validBillingKey = "SUCCESS_KEY"
        val hugeAmount = BigDecimal("10000000.00") // 10,000,000 이상 (Adapter의 로직에 의해 실패 유발)

        createPaymentMethodFixture(userId, validBillingKey)

        val command = AuthorizePaymentCommand(
            userId = userId,
            orderId = orderId,
            amount = hugeAmount,
        )

        shouldThrowExactly<PaymentFailedException> {
            paymentUseCase.authorizePayment(command)
        }.message shouldContain "결제 한도 초과"

        val failureRecord = paymentRepository.findByOrderId(orderId).orElse(null)

        failureRecord.shouldNotBeNull()
        failureRecord.status shouldBe PaymentStatus.FAILED
    }

    @Test
    fun `결제 승인 - 실패, PG사 타임아웃 발생 시 실패 기록이 남고 예외가 던져진다`() {
        val userId = 1L
        val orderId = 100L
        val failBillingKey = "FAIL_AUTH_TIMEOUT" // PaymentGatewayAdapter의 Mock 로직에 의해 Timeout 발생 유발

        createPaymentMethodFixture(userId, failBillingKey)

        val command = AuthorizePaymentCommand(
            userId = userId,
            orderId = orderId,
            amount = BigDecimal("15000.00"),
        )

        shouldThrowExactly<PaymentFailedException> {
            paymentUseCase.authorizePayment(command)
        }.message shouldContain "PG사 응답 지연"

        val failureRecord = paymentRepository.findByOrderId(orderId).orElse(null)

        failureRecord.shouldNotBeNull()
        failureRecord.status shouldBe PaymentStatus.FAILED
    }

    private fun createPaymentMethodFixture(userId: Long, billingKey: String): PaymentMethodJpaEntity {
        val entity = PaymentMethodJpaEntity(
            userId = userId,
            billingKey = billingKey,
            cardCompany = "TestBank",
            cardNumberMasked = "****-0000",
            isDefault = true
        )
        return paymentMethodRepository.save(entity)
    }
}