package com.chobolevel.domain

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan

// 엔티티 스캔을 맡는다. JPA 리포지토리(Spring Data) 스캔은 구현이 있는 infrastructure의 InfrastructureConfigurationLoader가 맡는다.
@AutoConfiguration
@ComponentScan
@EntityScan(basePackages = ["com.chobolevel.domain"])
class DomainConfigurationLoader
