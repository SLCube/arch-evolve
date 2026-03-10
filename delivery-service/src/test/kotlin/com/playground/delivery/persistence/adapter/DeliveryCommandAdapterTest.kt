package com.playground.delivery.persistence.adapter

import com.playground.delivery.domain.enum.DeliveryStatus
import com.playground.delivery.fixture.application.domain.DeliveryDomainTestFixture
import com.playground.delivery.persistence.entity.DeliveryJpaEntity
import com.playground.delivery.persistence.repository.DeliveryRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(DeliveryCommandAdapter::class)
class DeliveryCommandAdapterTest(
    @param:Autowired private val deliveryCommandAdapter: DeliveryCommandAdapter,
    @param:Autowired private val deliveryRepository: DeliveryRepository,
) {
    @Test
    fun `save 호출 시 Delivery 도메인 모델을 DB에 저장하고 ID가 할당된 객체를 반환해야 한다`() {
        // given
        val delivery =
            DeliveryDomainTestFixture.mockDelivery(
                id = null,
                orderId = 100L,
                userId = 2L,
                deliveryStatus = DeliveryStatus.PENDING,
            )

        // when
        val savedDelivery = deliveryCommandAdapter.save(delivery)

        // then
        savedDelivery.id shouldNotBe null
        savedDelivery.orderId shouldBe delivery.orderId
        savedDelivery.userId shouldBe delivery.userId
        savedDelivery.deliveryStatus shouldBe DeliveryStatus.PENDING

        val foundEntity = deliveryRepository.findById(savedDelivery.id!!).orElse(null)
        foundEntity shouldNotBe null
        foundEntity.orderId shouldBe delivery.orderId
        foundEntity.userId shouldBe delivery.userId
    }

    @Test
    fun `update 호출 시 배송 상태가 변경되어 저장되어야 한다`() {
        // given
        val pendingDelivery =
            DeliveryDomainTestFixture.mockDelivery(
                id = null,
                orderId = 200L,
                deliveryStatus = DeliveryStatus.PENDING,
            )
        val savedEntity = deliveryRepository.save(DeliveryJpaEntity.toJpaEntity(pendingDelivery))
        val shippingDelivery =
            DeliveryDomainTestFixture
                .mockDelivery(
                    id = savedEntity.id,
                    orderId = 200L,
                    deliveryStatus = DeliveryStatus.PENDING,
                ).also { it.startDelivery() }

        // when
        val updatedDelivery = deliveryCommandAdapter.update(shippingDelivery)

        // then
        updatedDelivery.deliveryStatus shouldBe DeliveryStatus.SHIPPING
        updatedDelivery.shippedAt shouldNotBe null

        val foundEntity = deliveryRepository.findByOrderId(200L).orElse(null)
        foundEntity shouldNotBe null
        foundEntity.status shouldBe DeliveryStatus.SHIPPING
    }
}
