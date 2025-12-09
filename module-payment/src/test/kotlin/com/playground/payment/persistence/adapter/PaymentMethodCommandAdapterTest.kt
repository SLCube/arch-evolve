package com.playground.payment.persistence.adapter

import com.playground.common.jpa.config.QuerydslConfig
import com.playground.payment.fixture.application.domain.PaymentMethodDomainTestFixture
import com.playground.payment.persistence.repository.PaymentMethodRepository
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(PaymentMethodCommandAdapter::class, QuerydslConfig::class)
class PaymentMethodCommandAdapterTest(
    @param:Autowired private val paymentMethodCommandAdapter: PaymentMethodCommandAdapter,
    @param:Autowired private val paymentMethodRepository: PaymentMethodRepository,
) {

    @Test
    fun `save 호출 시 PaymentMethod가 저장되고 ID가 부여된 도메인을 반환해야 한다`() {
        // given
        val paymentMethod = PaymentMethodDomainTestFixture.mockPaymentMethod(
            id = null,
            isDefault = true,
        )

        // when
        val savedMethod = paymentMethodCommandAdapter.save(paymentMethod)

        // then
        savedMethod.id shouldNotBe null
        savedMethod.cardCompany shouldBe paymentMethod.cardCompany
        savedMethod.isDefault shouldBe paymentMethod.isDefault

        val entity = paymentMethodRepository.findById(savedMethod.id!!).orElse(null)
        entity shouldNotBe null
        entity!!.billingKey shouldBe paymentMethod.billingKey
        entity.isDefault shouldBe true
    }

    @Test
    fun `delete 호출 시 해당 PaymentMethod가 삭제되어야 한다`() {
        // given
        val savedMethod = paymentMethodCommandAdapter.save(
            PaymentMethodDomainTestFixture.mockPaymentMethod(id = null, isDefault = true)
        )

        // when
        paymentMethodCommandAdapter.delete(savedMethod)

        // then
        paymentMethodRepository.findById(savedMethod.id!!).orElse(null).shouldBeNull()
    }
}
