package com.chobolevel.api.common.warmer

import org.springframework.web.client.RestTemplate

interface Warmer {

    fun warm(url: String, restTemplate: RestTemplate)
}
