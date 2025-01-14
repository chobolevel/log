package com.chobolevel.api

import com.chobolevel.domain.DomainConfigurationLoader
import com.ulisesbocchio.jasyptspringboot.environment.StandardEncryptableEnvironment
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableScheduling
@SpringBootApplication
@Import(DomainConfigurationLoader::class)
@ConfigurationPropertiesScan("com.chobolevel.api.properties")
class ApiApplication

fun main(args: Array<String>) {
    // logback jasypt 변수 사용하기 위해 ApiApplication 실행 시점을 변경
    SpringApplicationBuilder()
        .environment(StandardEncryptableEnvironment())
        .sources(ApiApplication::class.java)
        .run(*args)
}
