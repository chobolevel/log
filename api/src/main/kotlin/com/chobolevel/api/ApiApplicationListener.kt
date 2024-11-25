package com.chobolevel.api

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import java.net.InetAddress

@Component
class ApiApplicationListener(
    private val restTemplate: RestTemplate,
) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ApiApplication::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val url = "http://${InetAddress.getLocalHost().hostAddress}:9565"
        logger.info("===== warm up started with $url ====")
        restTemplate.getForEntity<String>("$url/api/v1/posts")
        restTemplate.getForEntity<String>("$url/api/v1/posts/1")
        logger.info("===== warm up ended with $url ======")
    }
}
