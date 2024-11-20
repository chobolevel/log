package com.chobolevel.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@Component
class ApiApplicationListener(
    private val restTemplate: RestTemplate,
    @Value("\${server.host}")
    private val host: String
) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ApiApplication::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        logger.info("===== warm up started with $host ====")
        restTemplate.getForEntity<String>("$host/api/v1/posts")
        logger.info("===== warm up ended with $host ======")
    }
}
