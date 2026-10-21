package com.chobolevel.api.common.container

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

/**
 * Singleton Container 패턴 (AbstractMySQLContainerTest와 같은 이유):
 * 객체가 처음 참조될 때 컨테이너를 한 번 기동하고, JVM 종료 시 정리된다.
 * 앱 설정(RedisConfiguration)이 비밀번호를 필수로 요구하므로 requirepass로 기동한다.
 */
object RedisTestContainer {

    const val PASSWORD: String = "test"
    private const val PORT: Int = 6379

    private val redis: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(PORT)
        .withCommand("redis-server", "--requirepass", PASSWORD)

    init {
        redis.start()
    }

    val host: String get() = redis.host
    val port: Int get() = redis.getMappedPort(PORT)
}
