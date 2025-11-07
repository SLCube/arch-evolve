package com.playground.product.service

import com.playground.product.domain.Product
import com.playground.product.repository.ProductRepository
import io.kotest.matchers.shouldBe
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
        val product = productRepository.save(Product(name = "테스트 상품", stock = 100))
        productId = requireNotNull(product.id) { "Product ID cannot be null in test setup." }
    }

    @AfterEach
    fun tearDown() {
        productRepository.deleteAll()
    }

    @Test
    fun `동시에 100개의 재고를 차감하면, 최종 재고는 0이 된다`() {
        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    productService.decreaseStock(productId, 1)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        val finalProduct = productRepository.findById(productId).get()
        finalProduct.stock shouldBe 0
    }
}