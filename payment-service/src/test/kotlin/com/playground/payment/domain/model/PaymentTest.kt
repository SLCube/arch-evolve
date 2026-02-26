package com.playground.payment.domain.model

import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.event.PaymentAuthorizedEvent
import com.playground.payment.domain.event.PaymentFailedEvent
import com.playground.payment.fixture.application.domain.PaymentDomainTestFixture
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class PaymentTest {
    @Test
    fun `complete 호출 시 PaymentAuthorizedEvent를 반환하고 상태가 COMPLETED로 변경되어야 한다`() {
        // given
        val payment = PaymentDomainTestFixture.mockPayment(status = PaymentStatus.PENDING)
        val pgTransactionId = "PG-TX-001"
        val approvalNumber = "APR-001"

        // when
        val result = payment.complete(pgTransactionId, approvalNumber)

        // then
        result.shouldBeInstanceOf<PaymentAuthorizedEvent>()
        result.orderId shouldBe payment.orderId
        result.userId shouldBe payment.userId
        result.amount shouldBe payment.amount
        result.pgTransactionId shouldBe pgTransactionId
        result.eventId shouldNotBe null

        payment.status shouldBe PaymentStatus.COMPLETED
        payment.pgTransactionId shouldBe pgTransactionId
        payment.approvalNumber shouldBe approvalNumber
        payment.approveAt shouldNotBe null
    }

    @Test
    fun `fail 호출 시 PaymentFailedEvent를 반환하고 상태가 FAILED로 변경되어야 한다`() {
        // given
        val payment = PaymentDomainTestFixture.mockPayment(status = PaymentStatus.PENDING)
        val reason = "한도 초과"

        // when
        val result = payment.fail(reason)

        // then
        result.shouldBeInstanceOf<PaymentFailedEvent>()
        result.failReason shouldBe reason
        result.orderId shouldBe payment.orderId
        result.userId shouldBe payment.userId
        result.eventId shouldNotBe null

        payment.status shouldBe PaymentStatus.FAILED
        payment.failReason shouldBe reason
    }

    @Test
    fun `fail에 null을 전달하면 기본 메시지로 대체되어야 한다`() {
        // given
        val payment = PaymentDomainTestFixture.mockPayment(status = PaymentStatus.PENDING)
        val defaultMessage = "PG사로부터 상세 오류 정보가 수신되지 않았습니다."

        // when
        val result = payment.fail(null)

        // then
        result.shouldBeInstanceOf<PaymentFailedEvent>()
        result.failReason shouldBe defaultMessage
        payment.failReason shouldBe defaultMessage
    }
}
