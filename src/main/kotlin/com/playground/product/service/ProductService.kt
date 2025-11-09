package com.playground.product.service

import com.playground.product.controller.response.ProductResponseDto
import com.playground.product.domain.Product
import com.playground.product.exception.ProductNotFoundException
import com.playground.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository
) {
    fun save(name: String, stock: Int, price: Long): ProductResponseDto {
        val product = Product(name = name, stock = stock, price = price)
        val savedProduct = productRepository.save(product)
        return ProductResponseDto.toResponse(savedProduct)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): ProductResponseDto {
        val product = productRepository.findById(id).orElseThrow { ProductNotFoundException(id) }
        return ProductResponseDto.toResponse(product)
    }

    @Transactional(readOnly = true)
    fun findAll(): List<ProductResponseDto> = productRepository.findAll()
        .map { ProductResponseDto.toResponse(it) }

    fun update(id: Long, name: String, stock: Int, price: Long): ProductResponseDto {
        val foundProduct = productRepository.findById(id).orElseThrow { ProductNotFoundException(id) }
        foundProduct.update(name, stock, price)

        return ProductResponseDto.toResponse(foundProduct)
    }

    fun decreaseStock(id: Long, quantity: Int): ProductResponseDto {
        val foundProduct = productRepository.findByIdWithPessimisticLock(id).orElseThrow { ProductNotFoundException(id) }
        foundProduct.decreaseStock(quantity)
        return ProductResponseDto.toResponse(foundProduct)
    }
}