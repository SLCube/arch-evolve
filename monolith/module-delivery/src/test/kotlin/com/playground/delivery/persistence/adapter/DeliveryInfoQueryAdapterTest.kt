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
@Import(DeliveryInfoQueryAdapter::class)
class DeliveryInfoQueryAdapterTest(
    @param:Autowired private val deliveryInfoQueryAdapter: DeliveryInfoQueryAdapter,
    @param:Autowired private val deliveryRepository: DeliveryRepository,
) {

    @Test
    fun `주문 ID로 DeliveryInfo를 조회할 수 있어야 한다`() {
        // given
        val delivery =
            DeliveryDomainTestFixture.createDelivery(
                id = null,
                orderId = 10L,
                userId = 20L,
                receiverName = "홍길동",
                receiverPhoneNumber = "010-1111-2222",
                zipCode = "12345",
                baseAddress = "서울시 테스트구",
                detailAddress = "101동 202호",
            )
        deliveryRepository.save(DeliveryJpaEntity.toJpaEntity(delivery))

        // when
        val result = deliveryInfoQueryAdapter.getDeliveryInfoByOrderId(10L)

        // then
        result.orderId shouldBe 10L
        result.userId shouldBe 20L
        result.receiverName shouldBe "홍길동"
        result.receiverPhoneNumber shouldBe "010-1111-2222"
        result.zipCode shouldBe "12345"
        result.baseAddress shouldBe "서울시 테스트구"
        result.detailAddress shouldBe "101동 202호"
        result.deliveryStatus shouldBe "PENDING"
    }

    @Test
    fun `존재하지 않는 주문 ID로 조회 시 DeliveryNotFoundException을 던져야 한다`() {
        // when & then
        shouldThrow<DeliveryNotFoundException> {
            deliveryInfoQueryAdapter.getDeliveryInfoByOrderId(999L)
        }
    }
}

