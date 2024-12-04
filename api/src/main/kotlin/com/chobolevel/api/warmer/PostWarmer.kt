package com.chobolevel.api.warmer

import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@Component
class PostWarmer : Warmer {

    override fun warm(url: String, restTemplate: RestTemplate) {
        restTemplate.getForEntity<String>("$url/api/v1/posts")
    }
}
