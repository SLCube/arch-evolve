package com.playground.product.application.service

import com.playground.product.application.port.`in`.ProductUseCase
import com.playground.product.application.port.`in`.command.DecreaseStockCommand
import com.playground.product.application.port.`in`.command.SaveProductCommand
import com.playground.product.application.port.`in`.command.UpdateProductCommand
import com.playground.product.application.port.`in`.query.GetProductQuery
import com.playground.product.application.port.out.ProductCommandPort
import com.playground.product.application.port.out.ProductQueryPort
import com.playground.product.domain.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productCommandPort: ProductCommandPort,
    private val productQueryPort: ProductQueryPort
): ProductUseCase {

    override fun saveProduct(command: SaveProductCommand): Product {
        val product = Product(
            name = command.name,
            stock = command.stock,
            price = command.price
        )

        return productCommandPort.save(product)
    }

    override fun updateProduct(command: UpdateProductCommand): Product {
        val product = productQueryPort.findById(command.id)

        product.update(
            name = command.name,
            stock = command.stock,
            price = command.price
        )

        return productCommandPort.update(product)
    }

    @Transactional(readOnly = true)
    override fun getProduct(query: GetProductQuery): Product {
        return productQueryPort.findById(query.id)
    }

    @Transactional(readOnly = true)
    override fun getAllProducts(): List<Product> {
        return productQueryPort.findAll()
    }

    override fun decreaseStock(command: DecreaseStockCommand): Product {
        val product = productQueryPort.findByIdWithPessimisticLock(command.id)

        product.decreaseStock(command.quantity)

        return productCommandPort.update(product)
    }
}