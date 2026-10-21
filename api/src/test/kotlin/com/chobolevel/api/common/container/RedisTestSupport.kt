package com.chobolevel.api.common.container

import com.chobolevel.api.common.redis.RedisConfiguration
import org.redisson.api.RedissonClient
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * Redis 컨테이너에 붙는 클라이언트를 운영과 같은 구성으로 만든다. 스프링 컨텍스트 없이 Provider를 직접 생성해 계약을 검증할 때 쓴다.
 * JVM당 한 번만 만든다.
 *
 * 주의: RedisCacheProvider에 실제로 주입되는 템플릿은 RedisConfiguration의 JSON 직렬화 템플릿(`redisTemplate`)이 아니라
 * Boot가 자동 구성하는 StringRedisTemplate이다. `RedisTemplate<String, String>` 타입으로 주입받을 때
 * 제네릭 변수(T)가 있는 `@Primary` 빈은 후보에서 빠지기 때문이다(전체 컨텍스트에서 실제 동작으로 확인, ApplicationContextSmokeTest 참고).
 * 그래서 여기서도 StringRedisTemplate을 쓴다.
 */
object RedisTestSupport {

    private val configuration: RedisConfiguration = RedisConfiguration(
        redisHost = RedisTestContainer.host,
        redisPort = RedisTestContainer.port,
        password = RedisTestContainer.PASSWORD
    )

    private val connectionFactory: LettuceConnectionFactory = (configuration.redisConnectionFactory() as LettuceConnectionFactory)
        .also { it.afterPropertiesSet() }

    // Provider가 실제로 쓰는 템플릿(문자열 직렬화)
    val template: StringRedisTemplate = StringRedisTemplate(connectionFactory)
        .also { it.afterPropertiesSet() }

    val redisson: RedissonClient = configuration.redissonClient()
        .also { client -> Runtime.getRuntime().addShutdownHook(Thread { client.shutdown() }) }
}
