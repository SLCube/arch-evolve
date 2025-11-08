package com.playground.support.docs

import com.playground.support.ApiTest
import com.playground.support.builder.ApiTestBuilder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.web.servlet.ResultActions

/**
 * Spring REST Docs 테스트를 위한 DSL (Domain-Specific Language)을 제공합니다.
 * API 테스트와 문서화를 하나의 선언적인 함수로 통합하여,
 * 테스트 코드의 가독성과 유지보수성을 극대화하는 것을 목표로 합니다.
 *
 * API 테스트 및 문서화를 한번에 수행하는 메인 함수.
 *
 * 사용 예시:
 * ```
 * performAndDocument("user-update-nickname-success") {
 *     httpMethod = HttpMethod.PATCH
 *     urlTemplate = "/users/{userId}/nickname"
 *     // ...
 * }
 * ```
 *
 * @param identifier 생성될 문서 스니펫의 고유 식별자.
 * @param builderBlock API 명세(호출, 검증, 문서화)를 정의하는 DSL 블록.
 */
fun ApiTest.performAndDocument(
    identifier: String,
    builderBlock: ApiTestBuilder.() -> Unit
) {
    val builder = ApiTestBuilder().apply(builderBlock)

    // URL 경로 변수에 null이 들어오는 것을 방지하여, 테스트 실패 원인을 명확히 함.
    val nonNullUrlVars =
        builder.urlVars.map { it ?: throw IllegalArgumentException("URL 변수는 null일 수 없습니다.") }.toTypedArray()

    // Spring REST Docs가 URL 템플릿을 인식할 수 있도록, 표준 MockMvcRequestBuilders가 아닌 RestDocumentationRequestBuilders를 사용.
    val requestBuilder = when (builder.httpMethod) {
        HttpMethod.POST -> RestDocumentationRequestBuilders.post(builder.urlTemplate, *nonNullUrlVars)
        HttpMethod.GET -> RestDocumentationRequestBuilders.get(builder.urlTemplate, *nonNullUrlVars)
        HttpMethod.PUT -> RestDocumentationRequestBuilders.put(builder.urlTemplate, *nonNullUrlVars)
        HttpMethod.DELETE -> RestDocumentationRequestBuilders.delete(builder.urlTemplate, *nonNullUrlVars)
        HttpMethod.PATCH -> RestDocumentationRequestBuilders.patch(builder.urlTemplate, *nonNullUrlVars)
        else -> throw IllegalArgumentException("Unsupported HTTP method: $builder.httpMethod")
    }

    val httpHeaders = HttpHeaders()
    // accessToken이 제공되면, Bearer 토큰 헤더를 자동으로 설정.
    if (builder.accessToken != null) {
        httpHeaders.setBearerAuth(builder.accessToken!!)
    }

    requestBuilder.apply {
        if (builder.requestBody != null) {
            contentType(MediaType.APPLICATION_JSON)
            content(objectMapper.writeValueAsString(builder.requestBody))
        }
        queryParams(builder.queryParamsInternal)
        headers(httpHeaders)
    }

    val resultActions = restDocsMockMvc.perform(requestBuilder)
        .andExpect(builder.expectedStatus)

    // 추가적인 검증 로직(jsonPath 등)을 동적으로 적용.
    builder.additionalMatchers.forEach { matcher ->
        resultActions.andExpect(matcher)
    }

    // accessToken이 제공된 경우, Authorization 헤더에 대한 문서 스니펫을 자동으로 추가.
    val allSnippets = if (builder.accessToken != null) {
        builder.snippets + requestHeaders(
            headerWithName(HttpHeaders.AUTHORIZATION).description("인증 토큰 (Bearer)")
        )
    } else {
        builder.snippets
    }

    resultActions.andDocument(identifier, *allSnippets)
}

/**
 * ResultActions에 대한 확장 함수.
 * andDo(document(...))와 prettyPrint()를 한번에 처리하여 보일러플레이트를 줄임.
 */
fun ResultActions.andDocument(identifier: String, vararg snippets: Snippet): ResultActions {
    return this.andDo(
        document(
            identifier,
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            *snippets
        )
    )
}