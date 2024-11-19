package com.chobolevel.api

import com.chobolevel.domain.DomainConfigurationLoader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EntityScan(basePackages = ["com.chobolevel.domain"])
@EnableJpaRepositories(basePackages = ["com.chobolevel.domain"])
@Import(DomainConfigurationLoader::class)
@EnableAsync
@EnableScheduling
@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
