package com.chobolevel.api.common.redis

import com.chobolevel.api.common.container.RedisTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.TimeUnit

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-2): 이동 전에 CacheProvider 구현의 현재 계약을 실제 Redis 컨테이너로 고정한다.
@DisplayName("RedisCacheProvider 계약 테스트 (Redis 컨테이너)")
class RedisCacheProviderTest {

    private val cacheProvider: CacheProvider = RedisCacheProvider(RedisTestSupport.template)

    // 테스트끼리 키가 겹치지 않도록 매번 새 키를 만든다.
    private fun newKey(): String = "contract:cache:${UUID.randomUUID()}"

    @Test
    fun `저장한 값을 같은 키로 조회한다`() {
        // given
        val key: String = newKey()

        // when
        cacheProvider.put(key, "value")

        // then
        assertThat(cacheProvider.get(key)).isEqualTo("value")
    }

    @Test
    fun `없는 키를 조회하면 null이다`() {
        assertThat(cacheProvider.get(newKey())).isNull()
    }

    @Test
    fun `같은 키에 다시 저장하면 값을 덮어쓴다`() {
        // given
        val key: String = newKey()
        cacheProvider.put(key, "first")

        // when
        cacheProvider.put(key, "second")

        // then
        assertThat(cacheProvider.get(key)).isEqualTo("second")
    }

    @Test
    fun `만료 시간을 지정해 저장하면 시간이 지난 뒤 사라진다`() {
        // given
        val key: String = newKey()

        // when
        cacheProvider.put(key, "value", 1, TimeUnit.SECONDS)
        val before: String? = cacheProvider.get(key)
        Thread.sleep(1500)

        // then
        assertThat(before).isEqualTo("value")
        assertThat(cacheProvider.get(key)).isNull()
        assertThat(cacheProvider.hasKey(key)).isFalse
    }

    @Test
    fun `delete는 키를 지우고 hasKey는 존재 여부를 알려준다`() {
        // given
        val key: String = newKey()
        cacheProvider.put(key, "value")
        val existed: Boolean = cacheProvider.hasKey(key)

        // when
        cacheProvider.delete(key)

        // then
        assertThat(existed).isTrue
        assertThat(cacheProvider.hasKey(key)).isFalse
        assertThat(cacheProvider.get(key)).isNull()
    }

    @Test
    fun `mget은 키 순서대로 값을 반환하고 없는 키는 null이다`() {
        // given
        val first: String = newKey()
        val missing: String = newKey()
        val third: String = newKey()
        cacheProvider.put(first, "a")
        cacheProvider.put(third, "c")

        // when
        val results: List<String?> = cacheProvider.mget(listOf(first, missing, third))

        // then
        assertThat(results).containsExactly("a", null, "c")
    }

    @Test
    fun `mget에 빈 목록을 주면 빈 목록을 반환한다`() {
        assertThat(cacheProvider.mget(emptyList())).isEmpty()
    }

    @Test
    fun `putIfAbsent는 키가 없을 때만 저장하고 기존 값을 유지한다`() {
        // given
        val key: String = newKey()

        // when
        val first: Boolean = cacheProvider.putIfAbsent(key, "first")
        val second: Boolean = cacheProvider.putIfAbsent(key, "second")

        // then
        assertThat(first).isTrue
        assertThat(second).isFalse
        assertThat(cacheProvider.get(key)).isEqualTo("first")
    }

    @Test
    fun `만료 시간을 지정한 putIfAbsent도 키가 없을 때만 저장하고 시간이 지나면 다시 저장할 수 있다`() {
        // given
        val key: String = newKey()

        // when
        val first: Boolean = cacheProvider.putIfAbsent(key, "first", 1, TimeUnit.SECONDS)
        val whileAlive: Boolean = cacheProvider.putIfAbsent(key, "second", 1, TimeUnit.SECONDS)
        Thread.sleep(1500)
        val afterExpiry: Boolean = cacheProvider.putIfAbsent(key, "third", 1, TimeUnit.SECONDS)

        // then
        assertThat(first).isTrue
        assertThat(whileAlive).isFalse
        assertThat(afterExpiry).isTrue
        assertThat(cacheProvider.get(key)).isEqualTo("third")
    }

    @Test
    fun `increment와 decrement는 없는 키에서 0을 기준으로 시작한다`() {
        // given
        val incrementKey: String = newKey()
        val decrementKey: String = newKey()

        // when & then
        assertThat(cacheProvider.increment(incrementKey)).isEqualTo(1L)
        assertThat(cacheProvider.increment(incrementKey)).isEqualTo(2L)
        assertThat(cacheProvider.decrement(decrementKey)).isEqualTo(-1L)
        assertThat(cacheProvider.decrement(decrementKey)).isEqualTo(-2L)
    }

    @Test
    fun `increment와 decrement는 서로 상쇄된다`() {
        // given
        val key: String = newKey()

        // when
        cacheProvider.increment(key)
        cacheProvider.increment(key)
        val result: Long = cacheProvider.decrement(key)

        // then
        assertThat(result).isEqualTo(1L)
    }

    @Test
    fun `increment한 카운터는 get으로 문자열 숫자로 읽힌다`() {
        // given: 좋아요·조회수·팔로우 카운터가 increment로 쌓이고 get으로 읽히는 경로를 고정한다.
        val key: String = newKey()

        // when
        cacheProvider.increment(key)
        cacheProvider.increment(key)
        val read: String? = cacheProvider.get(key)

        // then
        assertThat(read).isEqualTo("2")
    }

    @Test
    fun `put한 숫자 문자열에도 increment할 수 있다`() {
        // given: 카운터를 DB 값으로 적재(put)한 뒤 증감하는 경로를 고정한다.
        val key: String = newKey()
        cacheProvider.put(key, "5")

        // when
        val incremented: Long = cacheProvider.increment(key)

        // then
        assertThat(incremented).isEqualTo(6L)
        assertThat(cacheProvider.get(key)).isEqualTo("6")
    }

    @Test
    fun `값은 직렬화 없이 문자열 그대로 저장된다`() {
        // given: 운영 Redis에 이미 쌓인 캐시와 호환되어야 하므로 저장 형식을 고정한다.
        val key: String = newKey()

        // when
        cacheProvider.put(key, "value")

        // then
        assertThat(RedisTestSupport.template.opsForValue().get(key)).isEqualTo("value")
    }
}
