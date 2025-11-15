package com.playground.product.application.service

import com.playground.product.application.port.`in`.ProductUseCase
import com.playground.product.application.port.`in`.command.DecreaseStockCommand
import com.playground.product.application.port.`in`.command.SaveProductCommand
import com.playground.product.application.port.`in`.command.UpdateProductCommand
import com.playground.product.application.port.`in`.query.GetProductQuery
import com.playground.product.application.port.out.ProductCommandPort
import com.playground.product.application.port.out.ProductEventPort
import com.playground.product.application.port.out.ProductQueryPort
import com.playground.product.domain.Product
import com.playground.product.domain.event.ProductCreatedEvent
import com.playground.product.domain.event.ProductStockDecreasedEvent
import com.playground.product.domain.event.ProductUpdatedEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productCommandPort: ProductCommandPort,
    private val productQueryPort: ProductQueryPort,
    private val productEventPort: ProductEventPort,
) : ProductUseCase {
    override fun saveProduct(command: SaveProductCommand): Product {
        val product =
            Product(
                name = command.name,
                stock = command.stock,
                price = command.price,
            )

        val savedProduct = productCommandPort.save(product)

        val event =
            ProductCreatedEvent(
                productId = savedProduct.id!!,
                name = savedProduct.name,
                stock = savedProduct.stock,
                price = savedProduct.price,
            )
        productEventPort.publish(event)

        return savedProduct
    }

    override fun updateProduct(command: UpdateProductCommand): Product {
        val product = productQueryPort.findById(command.id)

        val oldName = product.name
        val oldStock = product.stock
        val oldPrice = product.price

        product.update(
            name = command.name,
            stock = command.stock,
            price = command.price,
        )

        val updatedProduct = productCommandPort.update(product)

        val event =
            ProductUpdatedEvent(
                productId = updatedProduct.id!!,
                oldName = oldName,
                newName = updatedProduct.name,
                oldStock = oldStock,
                newStock = updatedProduct.stock,
                oldPrice = oldPrice,
                newPrice = updatedProduct.price,
            )
        productEventPort.publish(event)

        return updatedProduct
    }

    @Transactional(readOnly = true)
    override fun getProduct(query: GetProductQuery): Product = productQueryPort.findById(query.id)

    @Transactional(readOnly = true)
    override fun getAllProducts(): List<Product> = productQueryPort.findAll()

    override fun decreaseStock(command: DecreaseStockCommand): Product {
        val product = productQueryPort.findByIdWithPessimisticLock(command.id)

        val oldStock = product.stock
        val decreasedQuantity = command.quantity

        product.decreaseStock(decreasedQuantity)

        val updatedProduct = productCommandPort.update(product)

        val event =
            ProductStockDecreasedEvent(
                productId = updatedProduct.id!!,
                productName = updatedProduct.name,
                oldStock = oldStock,
                decreasedQuantity = decreasedQuantity,
                newStock = updatedProduct.stock,
            )
        productEventPort.publish(event)

        return updatedProduct
    }
}
