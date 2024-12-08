package com.chobolevel.api

import com.chobolevel.api.warmer.Warmer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.InetAddress

@Component
class ApiApplicationListener(
    private val warmers: List<Warmer>,
    @Qualifier("baseRestTemplate")
    private val restTemplate: RestTemplate,
) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ApiApplication::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val url = "http://${InetAddress.getLocalHost().hostAddress}:9565"
        logger.info("===== warm up started with $url ====")
        warmers.forEach { it.warm(url = url, restTemplate = restTemplate) }
        logger.info("===== warm up ended with $url ======")
    }
}
