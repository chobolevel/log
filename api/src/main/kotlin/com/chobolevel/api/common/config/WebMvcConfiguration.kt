package com.chobolevel.api.common.config

import com.chobolevel.api.common.resolver.QueryObjectArgumentResolver
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfiguration(
    private val objectMapper: ObjectMapper
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(QueryObjectArgumentResolver(objectMapper))
    }
}
