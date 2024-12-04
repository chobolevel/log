package com.chobolevel.api.warmer

import org.springframework.web.client.RestTemplate

interface Warmer {

    fun warm(url: String, restTemplate: RestTemplate)
}
