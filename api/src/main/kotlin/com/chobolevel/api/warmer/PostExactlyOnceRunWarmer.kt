package com.chobolevel.api.warmer

import com.chobolevel.api.controller.v1.post.PostController
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@Component
class PostExactlyOnceRunWarmer(
    private val restTemplate: RestTemplate,
    @Value("\${server.protocol}")
    private val serverProtocol: String,
    @Value("\${server.host}")
    private val serverHost: String
) : ExactlyOnceRunWarmer() {

    private val logger = LoggerFactory.getLogger(PostExactlyOnceRunWarmer::class.java)

    override suspend fun doRun() {
        logger.info("==================================== warm up started ====================================")
        restTemplate.getForEntity<String>("$serverProtocol://$serverHost/api/v1/posts")
        logger.info("==================================== warm up ended ====================================")
    }
}
