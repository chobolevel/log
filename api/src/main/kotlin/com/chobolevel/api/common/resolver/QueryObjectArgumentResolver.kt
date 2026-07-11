package com.chobolevel.api.common.resolver

import com.chobolevel.api.common.annotation.QueryObject
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class QueryObjectArgumentResolver(
    objectMapper: ObjectMapper
) : HandlerMethodArgumentResolver {

    // 단일 값 query param도 servlet이 Array<String>으로 전달하므로 항상 toList()로 변환한다.
    // String? 등 scalar 필드에 단일 원소 리스트가 들어올 때 unwrap하기 위해 전역 ObjectMapper를 복사해 사용한다.
    private val mapper: ObjectMapper = objectMapper.copy()
        .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(QueryObject::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val paramMap: Map<String, Any> = webRequest.parameterMap
            .entries
            .associate { (key, values) -> key to values.toList() }
        return mapper.convertValue(paramMap, parameter.parameterType)
    }
}
