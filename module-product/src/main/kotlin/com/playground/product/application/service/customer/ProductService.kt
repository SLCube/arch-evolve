package com.playground.product.application.service.customer

import com.playground.product.application.port.inbound.ProductUseCase
import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.ProductSaveCommand
import com.playground.product.application.port.inbound.command.ProductUpdateCommand
import com.playground.product.application.port.inbound.query.ProductGetQuery
import com.playground.product.application.port.outbound.ProductCommandPort
import com.playground.product.application.port.outbound.ProductEventPort
import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.domain.event.ProductCreatedEvent
import com.playground.product.domain.event.ProductStockDecreasedEvent
import com.playground.product.domain.event.ProductUpdatedEvent
import com.playground.product.domain.exception.InsufficientStockException
import com.playground.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productCommandPort: ProductCommandPort,
    private val productQueryPort: ProductQueryPort,
    private val productEventPort: ProductEventPort,
) : ProductUseCase {
    override fun saveProduct(command: ProductSaveCommand): Product {
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

    override fun updateProduct(command: ProductUpdateCommand): Product {
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
    override fun getProduct(query: ProductGetQuery): Product = productQueryPort.findById(query.id)

    @Transactional(readOnly = true)
    override fun getAllProducts(): List<Product> = productQueryPort.findAll()

    override fun decreaseStock(command: DecreaseStockCommand): Long {
        val product = productQueryPort.findById(command.id)

        val decreasedQuantity = command.quantity

        val result = productCommandPort.decreaseStock(product, decreasedQuantity)

        val productId = product.id!!
        if (result <= 0) {
            throw InsufficientStockException(productId,  decreasedQuantity)
        }


        val event =
            ProductStockDecreasedEvent(
                productId = productId,
                productName = product.name,
                decreasedQuantity = decreasedQuantity,
            )
        productEventPort.publish(event)

        return productId
    }
}