package com.playground.common.application.query

data class PagedResult<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    fun <R> map(transform: (T) -> R): PagedResult<R> {
        val mappedContent = this.content.map(transform)
        return PagedResult(
            content = mappedContent,
            pageNumber = this.pageNumber,
            pageSize = this.pageSize,
            totalElements = this.totalElements,
            totalPages = this.totalPages,
        )
    }
}
