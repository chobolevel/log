package com.chobolevel.api

import com.chobolevel.api.controller.v1.post.PostController
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.getForObject

@Component
class ApiApplicationListener(
    private val restTemplate: RestTemplate,
    @Value("\${server.protocol}")
    private val serverProtocol: String,
    @Value("\${server.host}")
    private val serverHost: String,
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        restTemplate.getForEntity<String>("$serverProtocol://$serverHost/api/v1/posts")
    }
}
