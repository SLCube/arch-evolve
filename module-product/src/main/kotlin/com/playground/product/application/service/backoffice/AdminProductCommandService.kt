package com.playground.product.application.service.backoffice

import com.playground.product.application.port.outbound.ProductCommandPort
import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.contract.application.port.outbound.backoffice.AdminProductCommandUseCase
import com.playground.product.contract.application.port.outbound.backoffice.command.AdminProductSaveCommand
import com.playground.product.contract.application.port.outbound.backoffice.command.AdminProductUpdateCommand
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class AdminProductCommandService(
    private val productCommandPort: ProductCommandPort,
    private val productQueryPort: ProductQueryPort,
): AdminProductCommandUseCase {
    override fun saveProduct(command: AdminProductSaveCommand) {
        TODO("Not yet implemented")
    }

    override fun updateProduct(command: AdminProductUpdateCommand) {
        TODO("Not yet implemented")
    }
}