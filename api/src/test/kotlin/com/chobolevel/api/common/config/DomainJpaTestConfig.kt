package com.chobolevel.api.common.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

// @DataJpaTest는 @SpringBootApplication 패키지(com.chobolevel.api) 기준으로 스캔한다.
// domain 리포지토리와 엔티티는 com.chobolevel.domain에 있으므로 명시적으로 지정한다.
// DomainConfigurationLoader를 그대로 import하면 @ComponentScan이 EmailUtils 등
// 비JPA 빈도 로드해 JavaMailSender 의존성 에러가 발생하므로 별도 설정 클래스를 사용한다.
@TestConfiguration
@EnableJpaRepositories(basePackages = ["com.chobolevel.domain"])
@EntityScan(basePackages = ["com.chobolevel.domain"])
class DomainJpaTestConfig
