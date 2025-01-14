package com.chobolevel.domain

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@ComponentScan
@EnableJpaRepositories(basePackages = ["com.chobolevel.domain.entity"])
@EntityScan(basePackages = ["com.chobolevel.domain.entity"])
class DomainConfigurationLoader
