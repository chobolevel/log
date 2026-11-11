package com.chobolevel.infrastructure.redis

import com.chobolevel.domain.record.provider.RecordSeenProvider
import com.chobolevel.domain.record.vo.RecordFeedRedisKey
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

// 조회자별 최근 조회 기록을 ZSET(점수=조회 시각, 멤버=기록 id)으로 기억한다. 상한과 TTL은 RecordFeedRedisKey의 상수다.
@Component
class RedisRecordSeenProvider(
    private val redisTemplate: StringRedisTemplate,
) : RecordSeenProvider {

    companion object {
        // 웜업이 조회 이력이 없는 조회자에게도 집합이 생기게 하는 표식 멤버. 기록 id는 양수라 겹치지 않고, 점수가 0이라 상한이 차면 가장 먼저 버려진다.
        private const val SENTINEL_MEMBER = "0"

        // 존재 확인과 쓰기를 원자적으로 처리한다. 집합이 없으면(웜업 전) 아무것도 쓰지 않는다.
        // ARGV: 점수, 멤버, 상한, TTL(초)
        private val MARK_SEEN_SCRIPT: DefaultRedisScript<Long> = DefaultRedisScript(
            """
            if redis.call('EXISTS', KEYS[1]) == 0 then return 0 end
            redis.call('ZADD', KEYS[1], ARGV[1], ARGV[2])
            redis.call('ZREMRANGEBYRANK', KEYS[1], 0, -(tonumber(ARGV[3]) + 1))
            redis.call('EXPIRE', KEYS[1], tonumber(ARGV[4]))
            return 1
            """.trimIndent(),
            Long::class.java
        )

        // 키가 없을 때만 표식 멤버를 넣고 짧은 만료 시간을 건다. 이미 있는 집합은 건드리지 않는다.
        // ARGV: 짧은 TTL(초)
        private val BEGIN_WARM_SCRIPT: DefaultRedisScript<Long> = DefaultRedisScript(
            """
            if redis.call('EXISTS', KEYS[1]) == 1 then return 0 end
            redis.call('ZADD', KEYS[1], 'NX', 0, '$SENTINEL_MEMBER')
            redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))
            return 1
            """.trimIndent(),
            Long::class.java
        )

        // 이미 있는 항목의 점수는 건드리지 않는다(NX). 표식 멤버를 넣어 이력이 없어도 집합이 생기게 한다.
        // 만료는 이력이 있으면 항상 갱신하고, 이력이 없는 웜업은 집합을 새로 만들 때만 건다(이미 있는 집합의 만료를 줄이지 않는다).
        // ARGV: 상한, TTL(초), 만료 갱신 여부(1/0), 이후 (점수, 멤버) 쌍
        private val WARM_SCRIPT: DefaultRedisScript<Long> = DefaultRedisScript(
            """
            local existed = redis.call('EXISTS', KEYS[1])
            redis.call('ZADD', KEYS[1], 'NX', 0, '$SENTINEL_MEMBER')
            for i = 4, #ARGV, 2 do
                redis.call('ZADD', KEYS[1], 'NX', ARGV[i], ARGV[i + 1])
            end
            redis.call('ZREMRANGEBYRANK', KEYS[1], 0, -(tonumber(ARGV[1]) + 1))
            if existed == 0 or ARGV[3] == '1' then
                redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))
            end
            return 1
            """.trimIndent(),
            Long::class.java
        )

        private val TTL_SECONDS: Long = TimeUnit.DAYS.toSeconds(RecordFeedRedisKey.RECORD_SEEN_TTL_DAYS)
        private val EMPTY_TTL_SECONDS: Long = TimeUnit.MINUTES.toSeconds(RecordFeedRedisKey.RECORD_SEEN_EMPTY_TTL_MINUTES)
    }

    override fun findSeen(viewerKey: String, recordIds: List<Long>): Set<Long>? {
        val key: String = RecordFeedRedisKey.recordSeen(viewerKey)
        if (redisTemplate.hasKey(key) != true) {
            return null
        }
        if (recordIds.isEmpty()) {
            return emptySet()
        }
        // ZMSCORE(Redis 6.2 이상): 후보의 점수를 한 번에 읽는다. 없는 멤버는 null이다.
        val scores: List<Double?> = redisTemplate.opsForZSet().score(key, *recordIds.map { it.toString() }.toTypedArray())
            ?: return emptySet()
        return recordIds.filterIndexed { index, _ -> scores[index] != null }.toSet()
    }

    override fun markSeen(viewerKey: String, recordId: Long) {
        redisTemplate.execute(
            MARK_SEEN_SCRIPT,
            listOf(RecordFeedRedisKey.recordSeen(viewerKey)),
            System.currentTimeMillis().toString(),
            recordId.toString(),
            RecordFeedRedisKey.RECORD_SEEN_MAX_SIZE.toString(),
            TTL_SECONDS.toString(),
        )
    }

    override fun beginWarm(viewerKey: String) {
        redisTemplate.execute(
            BEGIN_WARM_SCRIPT,
            listOf(RecordFeedRedisKey.recordSeen(viewerKey)),
            EMPTY_TTL_SECONDS.toString(),
        )
    }

    override fun warm(viewerKey: String, recordIdsMostRecentFirst: List<Long>) {
        val now: Long = System.currentTimeMillis()
        // 가장 최근 조회가 가장 높은 점수를 갖도록 순서만큼 1ms씩 낮춘다
        val scoreAndMember: List<String> = recordIdsMostRecentFirst
            .take(RecordFeedRedisKey.RECORD_SEEN_MAX_SIZE.toInt())
            .flatMapIndexed { index, recordId -> listOf((now - index).toString(), recordId.toString()) }
        val hasHistory: Boolean = scoreAndMember.isNotEmpty()
        redisTemplate.execute(
            WARM_SCRIPT,
            listOf(RecordFeedRedisKey.recordSeen(viewerKey)),
            RecordFeedRedisKey.RECORD_SEEN_MAX_SIZE.toString(),
            (if (hasHistory) TTL_SECONDS else EMPTY_TTL_SECONDS).toString(),
            if (hasHistory) "1" else "0",
            *scoreAndMember.toTypedArray(),
        )
    }
}
