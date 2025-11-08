package com.playground.support.builder

import org.springframework.http.HttpMethod
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.web.servlet.ResultMatcher

class ApiTestBuilder {
    lateinit var httpMethod: HttpMethod
    lateinit var urlTemplate: String
    var urlVars: Array<Any?> = emptyArray()
    var requestBody: Any? = null
    var accessToken: String? = null
    lateinit var expectedStatus: ResultMatcher
    var additionalMatchers: Array<ResultMatcher> = emptyArray()
    var snippets: Array<Snippet> = emptyArray()
}