package com.playground.common.presentation.response

import com.playground.common.application.query.PagedResult

data class PagedResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    companion object {
        fun <T, R> of(
            pagedResult: PagedResult<T>,
            transform: (T) -> R,
        ): PagedResponse<R> {
            val content = pagedResult.content.map(transform)
            return PagedResponse(
                content = content,
                pageNumber = pagedResult.pageNumber,
                pageSize = pagedResult.pageSize,
                totalElements = pagedResult.totalElements,
                totalPages = pagedResult.totalPages,
            )
        }
    }
}
