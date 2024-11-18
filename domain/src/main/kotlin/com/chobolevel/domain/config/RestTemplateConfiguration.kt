package com.chobolevel.domain.config

import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.util.concurrent.TimeUnit

@Configuration
class RestTemplateConfiguration {

    @Bean
    fun restTemplate(): RestTemplate {
        val factory = createRequestFactory(500, 3000)
        return RestTemplate(factory)
    }

    private fun createRequestFactory(
        connectionTimeout: Long,
        readTimeout: Long,
        dispatcher: Dispatcher? = Dispatcher()
    ): OkHttp3ClientHttpRequestFactory {
        val connectionPool = ConnectionPool(20, 10, TimeUnit.SECONDS)
        val client = OkHttpClient.Builder()
            .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
            .connectionPool(connectionPool)
            .dispatcher(dispatcher!!)
            .build()

        return OkHttp3ClientHttpRequestFactory(client)
    }
}
