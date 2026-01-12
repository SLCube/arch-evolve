package com.playground.product.application.service.backoffice

import com.playground.product.application.port.outbound.ProductCommandPort
import com.playground.product.application.port.outbound.ProductEventPort
import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.contract.application.port.inbound.backoffice.BackOfficeProductCommandUseCase
import com.playground.product.contract.application.port.inbound.backoffice.command.AdminProductSaveCommand
import com.playground.product.contract.application.port.inbound.backoffice.command.AdminProductUpdateCommand
import com.playground.product.domain.event.ProductCreatedEvent
import com.playground.product.domain.event.ProductUpdatedEvent
import com.playground.product.domain.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class BackOfficeProductCommandService(
    private val productCommandPort: ProductCommandPort,
    private val productQueryPort: ProductQueryPort,
    private val productEventPort: ProductEventPort,
): BackOfficeProductCommandUseCase {
    override fun saveProduct(command: AdminProductSaveCommand) {
        val product = Product(
            name = command.name,
            stock = command.stock,
            price = command.price,
        )

        val savedProduct = productCommandPort.save(product)

        val event = ProductCreatedEvent(
            productId = savedProduct.id!!,
            name = savedProduct.name,
            stock = savedProduct.stock,
            price = savedProduct.price,
        )

        productEventPort.publish(event)
    }

    override fun updateProduct(command: AdminProductUpdateCommand) {
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

        val event = ProductUpdatedEvent(
            productId = updatedProduct.id!!,
            oldName = oldName,
            newName = updatedProduct.name,
            oldStock = oldStock,
            newStock = updatedProduct.stock,
            oldPrice = oldPrice,
            newPrice = updatedProduct.price,
        )

        productEventPort.publish(event)
    }
}