package com.playground.support.docs

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

object ApiDocumentUtils {

    fun commonErrorResponseSnippet(): List<FieldDescriptor> {
        return listOf(
            fieldWithPath("code").type(JsonFieldType.STRING).description("에러 코드"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
            fieldWithPath("errors").type(JsonFieldType.OBJECT).description("유효성 검증 에러 목록 (유효성 검증 실패 시에만 존재)").optional()
        )
    }
}