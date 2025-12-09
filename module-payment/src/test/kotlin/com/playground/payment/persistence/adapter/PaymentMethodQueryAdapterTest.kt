package com.playground.payment.persistence.adapter

import com.playground.common.jpa.config.QuerydslConfig
import com.playground.payment.domain.exception.DefaultPaymentMethodNotFoundException
import com.playground.payment.domain.exception.PaymentMethodNotFoundException
import com.playground.payment.fixture.application.domain.PaymentMethodDomainTestFixture
import com.playground.payment.persistence.entity.PaymentMethodJpaEntity
import com.playground.payment.persistence.repository.PaymentMethodRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(PaymentMethodQueryAdapter::class, QuerydslConfig::class)
class PaymentMethodQueryAdapterTest(
    @param:Autowired private val paymentMethodQueryAdapter: PaymentMethodQueryAdapter,
    @param:Autowired private val paymentMethodRepository: PaymentMethodRepository,
) {

    @BeforeEach
    fun cleanRepository() {
        paymentMethodRepository.deleteAll()
    }

    @Test
    fun `findDefaultByUserId 호출 시 기본 결제수단을 반환해야 한다`() {
        // given
        val userId = 12L
        savePaymentMethod(userId = userId, isDefault = true)

        // when
        val defaultMethod = paymentMethodQueryAdapter.findDefaultByUserId(userId)

        // then
        defaultMethod.userId shouldBe userId
        defaultMethod.isDefault shouldBe true
    }

    @Test
    fun `findDefaultByUserId 호출 시 기본 결제수단이 없으면 DefaultPaymentMethodNotFoundException을 던져야 한다`() {
        shouldThrow<DefaultPaymentMethodNotFoundException> {
            paymentMethodQueryAdapter.findDefaultByUserId(999L)
        }
    }

    @Test
    fun `findDefaultOrNullByUserId 호출 시 존재하면 Optional에 담아 반환해야 한다`() {
        // given
        val userId = 15L
        savePaymentMethod(userId = userId, isDefault = true)

        // when
        val optionalMethod = paymentMethodQueryAdapter.findDefaultOrNullByUserId(userId)

        // then
        optionalMethod.isPresent shouldBe true
        optionalMethod.get().userId shouldBe userId
    }

    @Test
    fun `findAllByUserId 호출 시 사용자의 모든 결제수단을 반환해야 한다`() {
        // given
        val userId = 20L
        savePaymentMethod(userId = userId, isDefault = true, billingKey = "billing-1")
        savePaymentMethod(userId = userId, isDefault = false, billingKey = "billing-2")

        // when
        val paymentMethods = paymentMethodQueryAdapter.findAllByUserId(userId)

        // then
        paymentMethods shouldHaveSize 2
        paymentMethods.first().userId shouldBe userId
    }

    @Test
    fun `findById 호출 시 PaymentMethod를 반환해야 한다`() {
        // given
        val saved = savePaymentMethod(userId = 22L, isDefault = true)

        // when
        val found = paymentMethodQueryAdapter.findById(saved.id!!)

        // then
        found.id shouldBe saved.id
        found.cardCompany shouldBe saved.cardCompany
    }

    @Test
    fun `findById 호출 시 존재하지 않으면 PaymentMethodNotFoundException을 던져야 한다`() {
        shouldThrow<PaymentMethodNotFoundException> {
            paymentMethodQueryAdapter.findById(9999L)
        }
    }

    @Test
    fun `countByUserId 호출 시 사용자의 결제수단 수를 반환해야 한다`() {
        // given
        val userId = 30L
        savePaymentMethod(userId = userId, isDefault = true, billingKey = "billing-1")
        savePaymentMethod(userId = userId, isDefault = false, billingKey = "billing-2")

        // when
        val count = paymentMethodQueryAdapter.countByUserId(userId)

        // then
        count shouldBe 2
    }

    private fun savePaymentMethod(
        userId: Long,
        isDefault: Boolean,
        billingKey: String = "billing-key-$userId-${System.nanoTime()}",
    ) = paymentMethodRepository.save(
        PaymentMethodJpaEntity.toJpaEntity(
            PaymentMethodDomainTestFixture.mockPaymentMethod(
                id = null,
                userId = userId,
                billingKey = billingKey,
                isDefault = isDefault,
            )
        )
    )
}
