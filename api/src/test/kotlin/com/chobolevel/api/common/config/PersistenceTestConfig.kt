package com.chobolevel.api.common.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

// @DataJpaTest는 @SpringBootApplication 패키지(com.chobolevel.api) 기준으로 스캔한다.
// 엔티티는 com.chobolevel.domain에, Spring Data 리포지토리 구현은 com.chobolevel.infrastructure에 있으므로 명시적으로 지정한다.
// DomainConfigurationLoader/InfrastructureConfigurationLoader를 그대로 import하면 @ComponentScan이 S3Configuration 등
// JPA와 무관한 빈까지 함께 올리므로, 슬라이스를 JPA로 한정하는 별도 설정 클래스를 사용한다.
@TestConfiguration
@EnableJpaRepositories(basePackages = ["com.chobolevel.infrastructure"])
@EntityScan(basePackages = ["com.chobolevel.domain"])
class PersistenceTestConfig
