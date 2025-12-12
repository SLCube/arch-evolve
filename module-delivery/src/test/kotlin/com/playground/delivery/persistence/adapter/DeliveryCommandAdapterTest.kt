package com.playground.delivery.persistence.adapter

import com.playground.delivery.domain.enum.DeliveryStatus
import com.playground.delivery.fixture.application.domain.DeliveryDomainTestFixture
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
    fun `배송 저장 시 도메인 모델이 DB에 저장되고 ID가 할당되어야 한다`() {
        // given
        val delivery = DeliveryDomainTestFixture.createDelivery(id = null)

        // when
        val savedDelivery = deliveryCommandAdapter.save(delivery)

        // then
        savedDelivery.id shouldNotBe null
        savedDelivery.deliveryStatus shouldBe DeliveryStatus.PENDING

        val foundEntity = deliveryRepository.findById(savedDelivery.id!!).orElse(null)
        foundEntity shouldNotBe null
        foundEntity!!.orderId shouldBe delivery.orderId
        foundEntity.deliveryReceiver.receiverName shouldBe delivery.deliveryReceiver.receiverName
        foundEntity.deliveryAddress.zipCode shouldBe delivery.deliveryAddress.zipCode
    }
}
