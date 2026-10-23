package com.chobolevel.infrastructure

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

// 이 모듈의 빈을 애플리케이션(api, 이후 worker)에 연결하는 진입점. META-INF/spring/...AutoConfiguration.imports로 등록한다.
// 영속성·외부 연동 구현이 이 모듈로 옮겨 오면(모듈 재편 5단계 S-6, S-7) 별도 설정 없이 자동으로 스캔된다.
// (엔티티 스캔은 엔티티가 있는 domain의 DomainConfigurationLoader가 맡는다.)
@AutoConfiguration
@ComponentScan
@EnableJpaRepositories(basePackages = ["com.chobolevel.infrastructure"])
@ConfigurationPropertiesScan("com.chobolevel.infrastructure")
class InfrastructureConfigurationLoader
