package com.playground.payment.persistence.adapter

import com.playground.payment.fixture.application.domain.PaymentDomainTestFixture
import com.playground.payment.persistence.config.QuerydslConfig
import com.playground.payment.persistence.entity.PaymentJpaEntity
import com.playground.payment.persistence.repository.PaymentRepository
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(PaymentQueryAdapter::class, QuerydslConfig::class)
class PaymentQueryAdapterTest(
    @param:Autowired private val paymentQueryAdapter: PaymentQueryAdapter,
    @param:Autowired private val paymentRepository: PaymentRepository,
) {
    @Test
    fun `findByOrderId 호출 시 저장된 Payment를 도메인 모델로 반환해야 한다`() {
        // given
        val paymentDomain =
            PaymentDomainTestFixture.mockPayment(
                id = null,
                orderId = 555L,
            )
        val savedEntity = paymentRepository.save(PaymentJpaEntity.toJpaEntity(paymentDomain))

        // when
        val foundPayment = paymentQueryAdapter.findByOrderId(paymentDomain.orderId)

        // then
        foundPayment shouldNotBe null
        foundPayment!!.id shouldBe savedEntity.id
        foundPayment.userId shouldBe paymentDomain.userId
    }

    @Test
    fun `findByOrderId 호출 시 존재하지 않는 주문이면 null을 반환해야 한다`() {
        // when
        val foundPayment = paymentQueryAdapter.findByOrderId(9999L)

        // then
        foundPayment.shouldBeNull()
    }
}
