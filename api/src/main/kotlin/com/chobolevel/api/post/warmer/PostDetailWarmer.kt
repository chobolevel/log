package com.chobolevel.api.post.warmer

import com.chobolevel.api.common.warmer.Warmer
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@Component
class PostDetailWarmer : Warmer {

    override fun warm(url: String, restTemplate: RestTemplate) {
        restTemplate.getForEntity<String>("$url/api/v1/posts/1")
    }
}
