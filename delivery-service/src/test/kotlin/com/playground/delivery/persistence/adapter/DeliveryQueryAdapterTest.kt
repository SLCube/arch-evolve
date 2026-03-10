package com.playground.delivery.persistence.adapter

import com.playground.delivery.domain.exception.DeliveryNotFoundException
import com.playground.delivery.fixture.application.domain.DeliveryDomainTestFixture
import com.playground.delivery.persistence.entity.DeliveryJpaEntity
import com.playground.delivery.persistence.repository.DeliveryRepository
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(DeliveryQueryAdapter::class)
class DeliveryQueryAdapterTest(
    @param:Autowired private val deliveryQueryAdapter: DeliveryQueryAdapter,
    @param:Autowired private val deliveryRepository: DeliveryRepository,
) {
    @Test
    fun `findByOrderId 호출 시 저장된 Delivery를 도메인 모델로 반환해야 한다`() {
        // given
        val deliveryDomain =
            DeliveryDomainTestFixture.mockDelivery(
                id = null,
                orderId = 555L,
                userId = 3L,
            )
        val savedEntity = deliveryRepository.save(DeliveryJpaEntity.toJpaEntity(deliveryDomain))

        // when
        val result = deliveryQueryAdapter.findByOrderId(555L)

        // then
        result.isPresent shouldBe true
        result.get().id shouldBe savedEntity.id
        result.get().userId shouldBe deliveryDomain.userId
        result.get().orderId shouldBe deliveryDomain.orderId
    }

    @Test
    fun `findByOrderId 호출 시 존재하지 않는 주문이면 빈 Optional을 반환해야 한다`() {
        // when
        val result = deliveryQueryAdapter.findByOrderId(9999L)

        // then
        result.isPresent shouldBe false
        result.orElse(null).shouldBeNull()
    }

    @Test
    fun `findByOrderIdOrThrow 호출 시 저장된 Delivery를 도메인 모델로 반환해야 한다`() {
        // given
        val deliveryDomain =
            DeliveryDomainTestFixture.mockDelivery(
                id = null,
                orderId = 777L,
                userId = 5L,
            )
        val savedEntity = deliveryRepository.save(DeliveryJpaEntity.toJpaEntity(deliveryDomain))

        // when
        val result = deliveryQueryAdapter.findByOrderIdOrThrow(777L)

        // then
        result shouldNotBe null
        result.id shouldBe savedEntity.id
        result.userId shouldBe deliveryDomain.userId
    }

    @Test
    fun `findByOrderIdOrThrow 호출 시 존재하지 않는 주문이면 DeliveryNotFoundException을 던져야 한다`() {
        // when / then
        assertThrows<DeliveryNotFoundException> {
            deliveryQueryAdapter.findByOrderIdOrThrow(9999L)
        }
    }
}
