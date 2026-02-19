package com.playground.support.docs

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.playground.support.RestDocsTest
import com.playground.support.builder.ApiTestBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.web.servlet.ResultActions

/**
 * Spring REST Docs 테스트를 위한 DSL (Domain-Specific Language)을 제공합니다.
 * API 테스트와 문서화를 하나의 선언적인 함수로 통합하여,
 * 테스트 코드의 가독성과 유지보수성을 극대화하는 것을 목표로 합니다.
 *
 * 사용 예시:
 * ```
 * performAndDocument("payment-method-register-success") {
 *     httpMethod = HttpMethod.POST
 *     urlTemplate = "/payment-methods"
 *     userId = 1L
 *     // ...
 * }
 * ```
 *
 * @param identifier 생성될 문서 스니펫의 고유 식별자.
 * @param builderBlock API 명세(호출, 검증, 문서화)를 정의하는 DSL 블록.
 */
fun RestDocsTest.performAndDocument(
    identifier: String,
    builderBlock: ApiTestBuilder.() -> Unit,
) {
    val builder = ApiTestBuilder().apply(builderBlock)

    val nonNullUrlVars =
        builder.urlVars.map { it ?: throw IllegalArgumentException("URL 변수는 null일 수 없습니다.") }.toTypedArray()

    val requestBuilder =
        when (builder.httpMethod) {
            HttpMethod.POST -> RestDocumentationRequestBuilders.post(builder.urlTemplate, *nonNullUrlVars)
            HttpMethod.GET -> RestDocumentationRequestBuilders.get(builder.urlTemplate, *nonNullUrlVars)
            HttpMethod.PUT -> RestDocumentationRequestBuilders.put(builder.urlTemplate, *nonNullUrlVars)
            HttpMethod.DELETE -> RestDocumentationRequestBuilders.delete(builder.urlTemplate, *nonNullUrlVars)
            HttpMethod.PATCH -> RestDocumentationRequestBuilders.patch(builder.urlTemplate, *nonNullUrlVars)
            else -> throw IllegalArgumentException("Unsupported HTTP method: $builder.httpMethod")
        }

    requestBuilder.apply {
        if (builder.requestBody != null) {
            contentType(MediaType.APPLICATION_JSON)
            content(objectMapper.writeValueAsString(builder.requestBody))
        }
        queryParams(builder.queryParamsInternal)
        if (builder.userId != null) {
            header("X-User-Id", builder.userId!!)
        }
    }

    val resultActions =
        restDocsMockMvc.perform(requestBuilder)
            .andExpect(builder.expectedStatus)

    builder.additionalMatchers.forEach { matcher ->
        resultActions.andExpect(matcher)
    }

    val allSnippets =
        if (builder.userId != null) {
            builder.snippets +
                requestHeaders(
                    headerWithName("X-User-Id").description("Gateway에서 전달된 사용자 ID"),
                )
        } else {
            builder.snippets
        }

    resultActions.andDocument(
        identifier = identifier,
        tag = builder.tag,
        summary = builder.summary,
        description = builder.description,
        snippets = allSnippets,
    )
}

fun ResultActions.andDocument(
    identifier: String,
    tag: String? = null,
    summary: String? = null,
    description: String? = null,
    snippets: Array<Snippet>,
): ResultActions {
    val resourceSnippet =
        resource(
            ResourceSnippetParameters.builder()
                .tag(tag ?: "API")
                .summary(summary ?: identifier)
                .description(description ?: "")
                .build(),
        )

    return this.andDo(
        MockMvcRestDocumentationWrapper.document(
            identifier,
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            resourceSnippet,
            *snippets,
        ),
    )
}
