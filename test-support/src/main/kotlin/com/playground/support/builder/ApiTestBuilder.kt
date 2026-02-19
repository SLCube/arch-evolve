package com.playground.support.builder

import org.springframework.http.HttpMethod
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class ApiTestBuilder {
    lateinit var httpMethod: HttpMethod
    lateinit var urlTemplate: String
    var urlVars: Array<Any?> = emptyArray()
    var requestBody: Any? = null
    internal val queryParamsInternal: MultiValueMap<String, String> = LinkedMultiValueMap()
    var userId: Long? = null
    lateinit var expectedStatus: ResultMatcher
    var additionalMatchers: Array<ResultMatcher> = emptyArray()
    var snippets: Array<Snippet> = emptyArray()

    var tag: String? = null
    var summary: String? = null
    var description: String? = null

    fun queryParams(block: MultiValueMap<String, String>.() -> Unit) {
        queryParamsInternal.apply(block)
    }
}
