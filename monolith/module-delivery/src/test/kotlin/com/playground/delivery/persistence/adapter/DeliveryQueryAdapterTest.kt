package com.playground.delivery.persistence.adapter

import com.playground.delivery.domain.exception.DeliveryNotFoundException
import com.playground.delivery.fixture.application.domain.DeliveryDomainTestFixture
import com.playground.delivery.persistence.entity.DeliveryJpaEntity
import com.playground.delivery.persistence.repository.DeliveryRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
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
    fun `주문 ID로 배송을 조회할 수 있어야 한다`() {
        // given
        val delivery = DeliveryDomainTestFixture.createDelivery(id = null, orderId = 10L)
        val deliveryJpaEntity = DeliveryJpaEntity.toJpaEntity(delivery)
        deliveryRepository.save(deliveryJpaEntity)

        // when
        val result = deliveryQueryAdapter.findByOrderIdOrThrow(10L)

        // then
        result.orderId shouldBe 10L
        result.deliveryReceiver.receiverName shouldBe delivery.deliveryReceiver.receiverName
    }

    @Test
    fun `존재하지 않는 주문 ID로 조회 시 Optional-empty가 반환되어야 한다`() {
        // when
        val result = deliveryQueryAdapter.findByOrderId(999L)

        // then
        result.isPresent shouldBe false
    }

    @Test
    fun `존재하지 않는 주문 ID로 조회 시 findByOrderIdOrThrow는 DeliveryNotFoundException을 던져야 한다`() {
        shouldThrow<DeliveryNotFoundException> {
            deliveryQueryAdapter.findByOrderIdOrThrow(999L)
        }
    }
}
