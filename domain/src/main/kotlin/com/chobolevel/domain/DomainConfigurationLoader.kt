package com.chobolevel.domain

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan

// 엔티티 스캔만 맡는다. domain에는 빈이 없으므로 컴포넌트 스캔은 하지 않는다. JPA 리포지토리(Spring Data) 스캔은 구현이 있는 infrastructure의 InfrastructureConfigurationLoader가 맡는다.
@AutoConfiguration
@EntityScan(basePackages = ["com.chobolevel.domain"])
class DomainConfigurationLoader
