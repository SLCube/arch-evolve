package com.playground.payment.application.service

import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.application.port.outbound.PaymentMethodCommandPort
import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.domain.exception.PaymentAccessDeniedException
import com.playground.payment.domain.model.PaymentMethod
import com.playground.payment.fixture.application.command.PaymentMethodCommandTestFixture
import com.playground.payment.fixture.application.domain.PaymentMethodDomainTestFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.*

@Suppress("NonAsciiCharacters")
class PaymentMethodServiceTest {

    private val paymentGatewayPort: PaymentGatewayPort = mock()
    private val paymentMethodCommandPort: PaymentMethodCommandPort = mock()
    private val paymentMethodQueryPort: PaymentMethodQueryPort = mock()

    private val paymentMethodService: PaymentMethodService = PaymentMethodService(
        paymentGatewayPort = paymentGatewayPort,
        paymentMethodCommandPort = paymentMethodCommandPort,
        paymentMethodQueryPort = paymentMethodQueryPort,
    )

    @Test
    fun `기본 결제수단 등록 시 기존 기본값을 해제하고 새 결제수단을 기본으로 저장해야 한다`() {
        // given
        val userId = 10L
        val command = PaymentMethodCommandTestFixture.registerCommand(
            userId = userId,
            setAsDefault = true,
        )
        val billingKey = "issued-billing-key"

        val existingDefault = PaymentMethodDomainTestFixture.mockPaymentMethod(
            id = 1L,
            userId = userId,
            isDefault = true,
        )

        given(paymentGatewayPort.issueBillingKey(eq(command.authKey), eq(command.userId)))
            .willReturn(billingKey)
        given(paymentMethodQueryPort.countByUserId(eq(userId)))
            .willReturn(2L)
        given(paymentMethodQueryPort.findDefaultOrNullByUserId(eq(userId)))
            .willReturn(Optional.of(existingDefault))

        stubSaveReturnsArgument()

        // when
        val result = paymentMethodService.registerPaymentMethod(command)

        // then
        existingDefault.isDefault shouldBe false

        verify(paymentGatewayPort).issueBillingKey(command.authKey, command.userId)
        verify(paymentMethodQueryPort).countByUserId(userId)
        verify(paymentMethodQueryPort).findDefaultOrNullByUserId(userId)

        verify(paymentMethodCommandPort).save(existingDefault)
        verify(paymentMethodCommandPort).save(
            check<PaymentMethod> { saved ->
                saved.userId shouldBe command.userId
                saved.billingKey shouldBe billingKey
                saved.isDefault shouldBe true
            }
        )

        result.billingKey shouldBe billingKey
        result.isDefault shouldBe true
    }

    @Test
    fun `첫 결제수단 등록 시 BillingKey를 발급받아 저장해야 한다`() {
        // given
        val userId = 5L
        val command = PaymentMethodCommandTestFixture.registerCommand(
            userId = userId,
            authKey = "first-auth-key",
            cardNumberMasked = "****-2222",
            setAsDefault = false,
        )
        val billingKey = "first-issued-billing-key"

        given(paymentGatewayPort.issueBillingKey(eq(command.authKey), eq(command.userId)))
            .willReturn(billingKey)
        given(paymentMethodQueryPort.countByUserId(eq(userId)))
            .willReturn(0L)
        given(paymentMethodQueryPort.findDefaultOrNullByUserId(eq(userId)))
            .willReturn(Optional.empty())
        stubSaveReturnsArgument()

        // when
        val result = paymentMethodService.registerPaymentMethod(command)

        // then
        verify(paymentGatewayPort).issueBillingKey(command.authKey, command.userId)
        verify(paymentMethodQueryPort).countByUserId(userId)
        verify(paymentMethodQueryPort).findDefaultOrNullByUserId(userId)

        verify(paymentMethodCommandPort).save(
            check<PaymentMethod> { saved ->
                saved.userId shouldBe command.userId
                saved.billingKey shouldBe billingKey
                saved.isDefault shouldBe true
            }
        )

        result.billingKey shouldBe billingKey
        result.cardCompany shouldBe command.cardCompany
        result.isDefault shouldBe true
    }

    @Test
    fun `사용자의 결제수단 목록 조회 시 저장소 결과를 그대로 반환해야 한다`() {
        // given
        val userId = 7L
        val paymentMethods = listOf(
            PaymentMethodDomainTestFixture.mockPaymentMethod(id = 1L, userId = userId, cardCompany = "카드A"),
            PaymentMethodDomainTestFixture.mockPaymentMethod(id = 2L, userId = userId, cardCompany = "카드B", isDefault = false),
        )

        given(paymentMethodQueryPort.findAllByUserId(eq(userId)))
            .willReturn(paymentMethods)

        // when
        val result = paymentMethodService.getPaymentMethodList(userId)

        // then
        result shouldHaveSize paymentMethods.size
        result shouldBe paymentMethods

        verify(paymentMethodQueryPort).findAllByUserId(userId)
    }

    @Test
    fun `본인 소유가 아닌 결제수단 삭제 시 PaymentAccessDeniedException을 던져야 한다`() {
        // given
        val userId = 1L
        val otherUserId = 2L
        val paymentMethodId = 11L
        val command = PaymentMethodCommandTestFixture.deleteCommand(
            userId = userId,
            paymentMethodId = paymentMethodId,
        )

        val otherUsersPaymentMethod = PaymentMethodDomainTestFixture.mockPaymentMethod(
            id = paymentMethodId,
            userId = otherUserId,
        )

        given(paymentMethodQueryPort.findById(eq(paymentMethodId)))
            .willReturn(otherUsersPaymentMethod)

        // when & then
        shouldThrow<PaymentAccessDeniedException> {
            paymentMethodService.deletePaymentMethod(command)
        }

        verify(paymentMethodQueryPort).findById(paymentMethodId)
        verify(paymentMethodCommandPort, never()).delete(any())
    }

    @Test
    fun `결제수단 삭제 시 사용자 소유가 맞다면 정상 삭제되어야 한다`() {
        // given
        val userId = 4L
        val paymentMethodId = 13L
        val command = PaymentMethodCommandTestFixture.deleteCommand(
            userId = userId,
            paymentMethodId = paymentMethodId,
        )

        val paymentMethod = PaymentMethodDomainTestFixture.mockPaymentMethod(
            id = paymentMethodId,
            userId = userId,
        )

        given(paymentMethodQueryPort.findById(eq(paymentMethodId)))
            .willReturn(paymentMethod)

        // when
        paymentMethodService.deletePaymentMethod(command)

        // then
        verify(paymentMethodQueryPort).findById(paymentMethodId)
        verify(paymentMethodCommandPort).delete(paymentMethod)
    }

    private fun stubSaveReturnsArgument() {
        given(paymentMethodCommandPort.save(any()))
            .willAnswer { invocation -> invocation.arguments[0] as PaymentMethod }
    }
}
