package com.playground.payment.persistence.adapter

import com.playground.common.jpa.config.QuerydslConfig
import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.fixture.application.domain.PaymentDomainTestFixture
import com.playground.payment.persistence.repository.PaymentRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(PaymentCommandAdapter::class, QuerydslConfig::class)
class PaymentCommandAdapterTest(
    @param:Autowired private val paymentCommandAdapter: PaymentCommandAdapter,
    @param:Autowired private val paymentRepository: PaymentRepository,
) {

    @Test
    fun `save 호출 시 Payment 도메인 모델을 DB에 저장하고 ID가 할당된 객체를 반환해야 한다`() {
        // given
        val payment = PaymentDomainTestFixture.mockPayment(
            id = null,
            status = PaymentStatus.PENDING,
            pgTransactionId = null,
            approvalNumber = null,
        )

        // when
        val savedPayment = paymentCommandAdapter.save(payment)

        // then
        savedPayment.id shouldNotBe null
        savedPayment.status shouldBe PaymentStatus.PENDING
        savedPayment.pgTransactionId shouldBe null

        val foundEntity = paymentRepository.findById(savedPayment.id!!).orElse(null)
        foundEntity shouldNotBe null
        foundEntity.orderId shouldBe payment.orderId
        foundEntity.amount shouldBe payment.amount
        foundEntity.usedPaymentKey shouldBe payment.usedPaymentKey
    }
}
