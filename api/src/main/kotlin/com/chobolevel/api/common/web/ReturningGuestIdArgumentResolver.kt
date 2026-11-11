package com.chobolevel.api.common.web

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class ReturningGuestIdArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(ReturningGuestId::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val request: HttpServletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java) ?: return null
        if (request.getAttribute(RequestAttributeKey.GUEST_ID_ISSUED) == true) {
            return null
        }
        return request.getAttribute(RequestAttributeKey.GUEST_ID) as? String
    }
}
