package com.playground.product.application.service.backoffice

import com.playground.product.application.port.outbound.ProductCommandPort
import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.contract.application.port.inbound.backoffice.BackOfficeProductCommandUseCase
import com.playground.product.contract.application.port.inbound.backoffice.command.AdminProductSaveCommand
import com.playground.product.contract.application.port.inbound.backoffice.command.AdminProductUpdateCommand
import com.playground.product.domain.model.Product
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class BackOfficeProductCommandService(
    private val productCommandPort: ProductCommandPort,
    private val productQueryPort: ProductQueryPort,
): BackOfficeProductCommandUseCase {
    override fun saveProduct(command: AdminProductSaveCommand) {
        val product = Product(
            name = command.name,
            stock = command.stock,
            price = command.price,
        )

        val savedProduct = productCommandPort.save(product)
    }

    override fun updateProduct(command: AdminProductUpdateCommand) {
        val product = productQueryPort.findById(command.id)

        product.update(
            name = command.name,
            stock = command.stock,
            price = command.price,
        )

        val updatedProduct = productCommandPort.update(product)
    }
}