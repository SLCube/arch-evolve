package com.playground.admin.product.service

import com.playground.product.contract.application.port.inbound.backoffice.BackOfficeProductCommandUseCase
import com.playground.product.contract.application.port.inbound.backoffice.command.AdminProductSaveCommand
import com.playground.product.contract.application.port.inbound.backoffice.command.AdminProductUpdateCommand
import org.springframework.stereotype.Service

@Service
class AdminProductFacade(
    private val backOfficeProductCommandUseCase: BackOfficeProductCommandUseCase,
) {

    fun saveProduct(command: AdminProductSaveCommand) {
        backOfficeProductCommandUseCase.saveProduct(command)
    }

    fun updateProduct(command: AdminProductUpdateCommand) {
        backOfficeProductCommandUseCase.updateProduct(command)
    }
}