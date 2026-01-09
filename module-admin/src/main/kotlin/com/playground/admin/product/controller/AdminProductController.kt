package com.playground.admin.product.controller

import com.playground.admin.product.controller.mapper.toCommand
import com.playground.admin.product.controller.request.AdminProductSaveRequestDto
import com.playground.admin.product.controller.request.AdminProductUpdateRequestDto
import com.playground.admin.product.service.AdminProductFacade
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/products")
class AdminProductController(
    private val adminProductFacade: AdminProductFacade,
) {
    @PostMapping
    fun save(
        @RequestBody @Valid requestDto: AdminProductSaveRequestDto,
    ): ResponseEntity<Unit> {
        adminProductFacade.saveProduct(requestDto.toCommand())
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid requestDto: AdminProductUpdateRequestDto,
    ): ResponseEntity<Unit> {
        adminProductFacade.updateProduct(requestDto.toCommand(id))
        return ResponseEntity.ok().build()
    }
}