package com.chobolevel.api.common.redis

import com.chobolevel.api.common.container.RedisTestSupport
import com.chobolevel.domain.record.provider.RecordSeenProvider
import com.chobolevel.domain.record.vo.RecordFeedRedisKey
import com.chobolevel.infrastructure.redis.RedisRecordSeenProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.TimeUnit

// 기록 피드(docs/design/record-feed.md, S-3): RecordSeenProvider 구현의 계약을 실제 Redis 컨테이너로 고정한다.
@DisplayName("RedisRecordSeenProvider 계약 테스트 (Redis 컨테이너)")
class RedisRecordSeenProviderTest {

    private val provider: RecordSeenProvider = RedisRecordSeenProvider(RedisTestSupport.template)

    // 테스트끼리 키가 겹치지 않도록 매번 새 조회자 키를 만든다.
    private fun newViewer(): String = "user:${UUID.randomUUID()}"

    private fun redisKey(viewerKey: String): String = RecordFeedRedisKey.recordSeen(viewerKey)

    @Test
    fun `집합이 없으면 findSeen은 null이다(콜드)`() {
        assertThat(provider.findSeen(newViewer(), listOf(1L, 2L))).isNull()
    }

    @Test
    fun `집합이 없을 때 markSeen은 집합을 만들지 않는다`() {
        // given
        val viewer: String = newViewer()

        // when
        provider.markSeen(viewer, 1L)

        // then: 웜업 없이 키가 생기면 일부만 든 집합이 웜 상태로 보인다
        assertThat(RedisTestSupport.template.hasKey(redisKey(viewer))).isFalse
        assertThat(provider.findSeen(viewer, listOf(1L))).isNull()
    }

    @Test
    fun `웜업하면 후보 중 본 기록만 반환한다`() {
        // given
        val viewer: String = newViewer()
        provider.warm(viewer, listOf(30L, 20L, 10L))

        // when & then
        assertThat(provider.findSeen(viewer, listOf(10L, 15L, 30L, 99L))).containsExactlyInAnyOrder(10L, 30L)
        assertThat(provider.findSeen(viewer, listOf(15L, 99L))).isEmpty()
    }

    @Test
    fun `조회 이력이 없어도 웜업하면 집합이 만들어져 콜드와 구분된다`() {
        // given
        val viewer: String = newViewer()

        // when
        provider.warm(viewer, emptyList())

        // then
        assertThat(provider.findSeen(viewer, listOf(1L, 2L))).isEmpty()
        assertThat(provider.findSeen(viewer, listOf(1L, 2L))).isNotNull
    }

    @Test
    fun `웜업 뒤에는 markSeen이 반영된다`() {
        // given
        val viewer: String = newViewer()
        provider.warm(viewer, listOf(1L))

        // when
        provider.markSeen(viewer, 5L)

        // then
        assertThat(provider.findSeen(viewer, listOf(1L, 5L, 9L))).containsExactlyInAnyOrder(1L, 5L)
    }

    @Test
    fun `같은 기록을 다시 기록해도 집합 크기가 늘지 않는다`() {
        // given
        val viewer: String = newViewer()
        provider.warm(viewer, listOf(1L))
        provider.markSeen(viewer, 5L)
        val sizeBefore: Long = RedisTestSupport.template.opsForZSet().zCard(redisKey(viewer))!!

        // when
        provider.markSeen(viewer, 5L)
        provider.markSeen(viewer, 5L)

        // then
        assertThat(RedisTestSupport.template.opsForZSet().zCard(redisKey(viewer))).isEqualTo(sizeBefore)
        assertThat(provider.findSeen(viewer, listOf(5L))).containsExactly(5L)
    }

    @Test
    fun `웜업은 이미 있는 항목의 더 최근 점수를 낮추지 않는다`() {
        // given: 웜업 뒤에 다시 조회해 점수가 최신이 됐다
        val viewer: String = newViewer()
        provider.warm(viewer, listOf(1L))
        provider.markSeen(viewer, 1L)
        val scoreBefore: Double = RedisTestSupport.template.opsForZSet().score(redisKey(viewer), "1")!!

        // when: 오래된 DB 이력으로 다시 웜업한다(동시 요청이 웜업을 겹쳐 하는 상황)
        provider.warm(viewer, listOf(1L))

        // then
        assertThat(RedisTestSupport.template.opsForZSet().score(redisKey(viewer), "1")).isEqualTo(scoreBefore)
    }

    @Test
    fun `상한을 넘으면 가장 오래된 것부터 버린다`() {
        // given: 상한만큼 웜업한다. 가장 최근 조회가 먼저이므로 목록의 마지막이 가장 오래된 기록이다.
        val viewer: String = newViewer()
        val max: Int = RecordFeedRedisKey.RECORD_SEEN_MAX_SIZE.toInt()
        val mostRecentFirst: List<Long> = (max.toLong() downTo 1L).toList()
        provider.warm(viewer, mostRecentFirst)
        assertThat(provider.findSeen(viewer, listOf(1L, max.toLong()))).containsExactlyInAnyOrder(1L, max.toLong())

        // when: 상한을 넘는 새 조회가 들어온다
        provider.markSeen(viewer, 100_000L)

        // then: 가장 오래된 기록이 빠지고 나머지와 새 기록은 남는다
        assertThat(RedisTestSupport.template.opsForZSet().zCard(redisKey(viewer))).isEqualTo(max.toLong())
        assertThat(provider.findSeen(viewer, listOf(1L, 2L, 100_000L, max.toLong()))).containsExactlyInAnyOrder(2L, 100_000L, max.toLong())
    }

    @Test
    fun `웜업과 기록 모두 만료 시간을 설정한다`() {
        // given
        val viewer: String = newViewer()
        val ttlSeconds: Long = TimeUnit.DAYS.toSeconds(RecordFeedRedisKey.RECORD_SEEN_TTL_DAYS)

        // when & then
        provider.warm(viewer, listOf(1L))
        assertThat(RedisTestSupport.template.getExpire(redisKey(viewer), TimeUnit.SECONDS)).isBetween(ttlSeconds - 60, ttlSeconds)

        RedisTestSupport.template.expire(redisKey(viewer), 100, TimeUnit.SECONDS)
        provider.markSeen(viewer, 2L)
        assertThat(RedisTestSupport.template.getExpire(redisKey(viewer), TimeUnit.SECONDS)).isBetween(ttlSeconds - 60, ttlSeconds)
    }

    @Test
    fun `조회 이력이 없는 웜업은 짧은 만료 시간으로 만든다`() {
        // given: 쿠키 없는 방문자가 요청마다 새 조회자가 되어도 키가 오래 쌓이지 않아야 한다
        val viewer: String = newViewer()
        val emptyTtlSeconds: Long = TimeUnit.MINUTES.toSeconds(RecordFeedRedisKey.RECORD_SEEN_EMPTY_TTL_MINUTES)

        // when
        provider.warm(viewer, emptyList())

        // then
        assertThat(RedisTestSupport.template.getExpire(redisKey(viewer), TimeUnit.SECONDS)).isBetween(emptyTtlSeconds - 60, emptyTtlSeconds)
    }

    @Test
    fun `이력이 없던 집합에 조회가 기록되면 만료 시간이 길어진다`() {
        // given
        val viewer: String = newViewer()
        val ttlSeconds: Long = TimeUnit.DAYS.toSeconds(RecordFeedRedisKey.RECORD_SEEN_TTL_DAYS)
        provider.warm(viewer, emptyList())

        // when
        provider.markSeen(viewer, 1L)

        // then
        assertThat(RedisTestSupport.template.getExpire(redisKey(viewer), TimeUnit.SECONDS)).isBetween(ttlSeconds - 60, ttlSeconds)
    }

    @Test
    fun `이미 긴 만료 시간을 가진 집합에 이력 없는 웜업이 겹쳐도 만료 시간을 줄이지 않는다`() {
        // given
        val viewer: String = newViewer()
        val ttlSeconds: Long = TimeUnit.DAYS.toSeconds(RecordFeedRedisKey.RECORD_SEEN_TTL_DAYS)
        provider.warm(viewer, listOf(1L))

        // when: 동시 요청이 이력 조회를 먼저 끝내 빈 목록으로 웜업하는 상황
        provider.warm(viewer, emptyList())

        // then
        assertThat(RedisTestSupport.template.getExpire(redisKey(viewer), TimeUnit.SECONDS)).isBetween(ttlSeconds - 60, ttlSeconds)
    }

    @Test
    fun `beginWarm은 표식만 든 집합을 짧은 만료 시간으로 만든다`() {
        // given
        val viewer: String = newViewer()
        val emptyTtlSeconds: Long = TimeUnit.MINUTES.toSeconds(RecordFeedRedisKey.RECORD_SEEN_EMPTY_TTL_MINUTES)

        // when
        provider.beginWarm(viewer)

        // then: 집합이 생겨 콜드와 구분되고, 아직 채워지지 않았으므로 본 기록은 없다
        assertThat(provider.findSeen(viewer, listOf(1L, 2L))).isEmpty()
        assertThat(RedisTestSupport.template.opsForZSet().zCard(redisKey(viewer))).isEqualTo(1L)
        assertThat(RedisTestSupport.template.getExpire(redisKey(viewer), TimeUnit.SECONDS)).isBetween(emptyTtlSeconds - 60, emptyTtlSeconds)
    }

    @Test
    fun `beginWarm은 이미 있는 집합의 내용과 만료 시간을 바꾸지 않는다`() {
        // given
        val viewer: String = newViewer()
        val ttlSeconds: Long = TimeUnit.DAYS.toSeconds(RecordFeedRedisKey.RECORD_SEEN_TTL_DAYS)
        provider.warm(viewer, listOf(1L))

        // when
        provider.beginWarm(viewer)

        // then
        assertThat(provider.findSeen(viewer, listOf(1L))).containsExactly(1L)
        assertThat(RedisTestSupport.template.getExpire(redisKey(viewer), TimeUnit.SECONDS)).isBetween(ttlSeconds - 60, ttlSeconds)
    }

    @Test
    fun `웜업이 DB를 읽는 사이 커밋된 조회의 markSeen이 beginWarm 이후이면 이후 warm이 와도 유지된다`() {
        // given: 경합 시퀀스 — 표식 생성 → (DB 읽기 중) 조회가 커밋되어 markSeen → 조회가 빠진 과거 이력으로 warm
        val viewer: String = newViewer()
        provider.beginWarm(viewer)

        // when
        provider.markSeen(viewer, 5L)
        provider.warm(viewer, listOf(1L))

        // then: 표식이 없었다면 markSeen이 건너뛰어져 5가 영영 빠진다
        assertThat(provider.findSeen(viewer, listOf(1L, 5L))).containsExactlyInAnyOrder(1L, 5L)
    }

    @Test
    fun `표식 없이 도착한 markSeen은 건너뛰고 그 뒤의 웜업이 DB 이력으로 채운다`() {
        // given: 표식 생성 전에 markSeen이 도착했다. 이 경우 DB 읽기가 커밋 이후라 이력에 이미 포함된다(beginWarm 이후에 읽기 때문).
        val viewer: String = newViewer()

        // when
        provider.markSeen(viewer, 5L)
        provider.beginWarm(viewer)
        provider.warm(viewer, listOf(5L))

        // then
        assertThat(provider.findSeen(viewer, listOf(5L))).containsExactly(5L)
    }

    @Test
    fun `조회자마다 집합이 독립적이다`() {
        // given
        val id: String = UUID.randomUUID().toString()
        val member: String = "user:$id"
        val guest: String = "guest:$id"
        provider.warm(member, listOf(1L))
        provider.warm(guest, emptyList())

        // when & then
        assertThat(provider.findSeen(member, listOf(1L))).containsExactly(1L)
        assertThat(provider.findSeen(guest, listOf(1L))).isEmpty()
    }

    @Test
    fun `후보가 비어 있으면 집합 존재 여부만 구분한다`() {
        // given
        val warm: String = newViewer()
        provider.warm(warm, listOf(1L))

        // when & then
        assertThat(provider.findSeen(warm, emptyList())).isEmpty()
        assertThat(provider.findSeen(newViewer(), emptyList())).isNull()
    }
}
