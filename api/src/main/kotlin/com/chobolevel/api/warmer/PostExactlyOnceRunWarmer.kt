package com.chobolevel.api.warmer

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@Component
class PostExactlyOnceRunWarmer(
    private val restTemplate: RestTemplate,
    @Value("\${server.host}")
    private val host: String
) : ExactlyOnceRunWarmer() {

    private val logger = LoggerFactory.getLogger(PostExactlyOnceRunWarmer::class.java)

    override suspend fun doRun() {
        logger.info("===== warm up started with $host ====")
        restTemplate.getForEntity<String>("$host/api/v1/posts")
        restTemplate.getForEntity<String>("$host/api/v1/posts/1")
        logger.info("===== warm up ended with $host ======")
    }
}
