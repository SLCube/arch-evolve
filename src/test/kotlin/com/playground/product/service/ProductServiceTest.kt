package com.playground.product.service

import com.playground.product.domain.Product
import com.playground.product.repository.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@Suppress("NonAsciiCharacters")
@SpringBootTest
class ProductServiceTest(
    @param:Autowired private val productService: ProductService,
    @param:Autowired private val productRepository: ProductRepository
) {

    private var productId: Long = 0L

    @BeforeEach
    fun setUp() {
        // 각 테스트 전에 새로운 상품을 저장하고 ID를 저장
        val product = productRepository.save(Product(name = "테스트 상품", stock = 100))
        productId = product.id!!
    }

    @AfterEach
    fun tearDown() {
        // 각 테스트 후에 저장된 상품 삭제
        productRepository.deleteAll()
    }

    @Test
    fun `상품을 저장한다`() {
        // given
        val name = "상품1"
        val stock = 10

        // when
        val savedProduct = productService.save(name, stock)

        // then
        assertThat(savedProduct.id).isNotNull()
        assertThat(savedProduct.name).isEqualTo(name)
        assertThat(savedProduct.stock).isEqualTo(stock)
    }

    @Test
    fun `상품을 조회한다`() {
        // BeforeEach에서 저장된 productId를 사용
        val foundProduct = productService.findById(productId)

        // then
        assertThat(foundProduct.name).isEqualTo("테스트 상품")
        assertThat(foundProduct.stock).isEqualTo(100)
    }

    @Test
    fun `동시에 100개의 재고를 차감한다`() {
        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(32) // 스레드 풀 생성
        val latch = CountDownLatch(threadCount) // 모든 스레드의 완료를 기다리기 위한 래치

        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    productService.decreaseStock(productId, 1) // 재고 1씩 차감
                } finally {
                    latch.countDown() // 작업 완료 시 래치 카운트 감소
                }
            }
        }

        latch.await() // 모든 스레드의 작업이 완료될 때까지 대기

        // 최종 재고를 확인하기 위해 Repository에서 다시 조회
        val finalProduct = productRepository.findById(productId).get()
        assertThat(finalProduct.stock).isZero() // 최종 재고가 0인지 검증
    }
}