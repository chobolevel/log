package com.chobolevel.api.common.resolver

import com.chobolevel.api.common.annotation.QueryObject
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class QueryObjectArgumentResolver(
    private val objectMapper: ObjectMapper
) : HandlerMethodArgumentResolver {

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
            .associate { (key, values) ->
                key to if (values.size == 1) values[0] else values.toList()
            }
        return objectMapper.convertValue(paramMap, parameter.parameterType)
    }
}
