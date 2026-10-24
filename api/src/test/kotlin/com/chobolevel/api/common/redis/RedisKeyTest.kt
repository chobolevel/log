package com.chobolevel.api.common.redis

import com.chobolevel.domain.record.like.vo.RecordLikeRedisKey
import com.chobolevel.domain.record.view.vo.RecordViewRedisKey
import com.chobolevel.domain.record.vo.RecordContributionRedisKey
import com.chobolevel.domain.user.follow.vo.UserFollowRedisKey
import com.chobolevel.domain.user.vo.UserRedisKey
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-5): 캐시 키를 domain으로 옮기기 전에 키 문자열을 그대로 고정한다.
// 운영 Redis에는 이미 이 형식의 키가 쌓여 있어서, 문자열이 한 글자라도 바뀌면 캐시가 갈라지고 기존 키는 고아가 된다.
// 기대값은 일부러 리터럴로 적는다(구현의 템플릿을 참조하지 않는다).
@DisplayName("Redis 키 문자열 고정 테스트")
class RedisKeyTest {

    @Test
    fun `user 키는 이전과 같은 문자열이다`() {
        assertThat(UserRedisKey.userEmailVerification("a@b.com")).isEqualTo("user:a@b.com:email-verification:v1")
        assertThat(UserRedisKey.userResetPassword("a@b.com")).isEqualTo("user:a@b.com:reset-password:v1")
        assertThat(UserRedisKey.userRefreshToken(1L)).isEqualTo("user:1:refresh-token:v1")
    }

    @Test
    fun `기록 잔디 키는 이전과 같은 문자열이다`() {
        assertThat(RecordContributionRedisKey.userRecordContribution(1L, 2026)).isEqualTo("user:1:record-contribution:2026:v1")
    }

    @Test
    fun `좋아요 키는 이전과 같은 문자열이다`() {
        assertThat(RecordLikeRedisKey.recordLikeCount(10L)).isEqualTo("record:10:like-count:v1")
        assertThat(RecordLikeRedisKey.recordLike(recordId = 10L, userId = 1L)).isEqualTo("record:10:like:1:v1")
    }

    @Test
    fun `조회수 키는 이전과 같은 문자열이다`() {
        assertThat(RecordViewRedisKey.recordViewCount(10L)).isEqualTo("record:10:view-count:v1")
        assertThat(RecordViewRedisKey.recordViewDedup(recordId = 10L, viewerKey = "user:1")).isEqualTo("record:10:view-dedup:user:1:v1")
        assertThat(RecordViewRedisKey.recordViewDedup(recordId = 10L, viewerKey = "guest:abc")).isEqualTo("record:10:view-dedup:guest:abc:v1")
    }

    @Test
    fun `팔로우 키는 이전과 같은 문자열이다`() {
        assertThat(UserFollowRedisKey.userFollowRelation(followerUserId = 1L, followingUserId = 2L)).isEqualTo("user:1:follow:2:v1")
        assertThat(UserFollowRedisKey.userFollowLock(followerUserId = 1L, followingUserId = 2L)).isEqualTo("user:1:follow-lock:2:v1")
        assertThat(UserFollowRedisKey.userFollowerCount(1L)).isEqualTo("user:1:follower-count:v1")
        assertThat(UserFollowRedisKey.userFollowingCount(1L)).isEqualTo("user:1:following-count:v1")
    }

    @Test
    fun `팔로우 키는 팔로워와 팔로잉의 순서가 키에 반영된다`() {
        assertThat(UserFollowRedisKey.userFollowRelation(followerUserId = 1L, followingUserId = 2L))
            .isNotEqualTo(UserFollowRedisKey.userFollowRelation(followerUserId = 2L, followingUserId = 1L))
        assertThat(UserFollowRedisKey.userFollowLock(followerUserId = 1L, followingUserId = 2L))
            .isNotEqualTo(UserFollowRedisKey.userFollowRelation(followerUserId = 1L, followingUserId = 2L))
    }

    @Test
    fun `경계값에서도 치환 결과가 이전과 같다`() {
        // 이메일에 구분자(:)가 들어가도 그대로 끼워 넣는다 / 큰 id / 큰 연도
        assertThat(UserRedisKey.userEmailVerification("a:b@c.com")).isEqualTo("user:a:b@c.com:email-verification:v1")
        assertThat(UserRedisKey.userRefreshToken(Long.MAX_VALUE)).isEqualTo("user:9223372036854775807:refresh-token:v1")
        assertThat(RecordContributionRedisKey.userRecordContribution(Long.MAX_VALUE, 9999)).isEqualTo("user:9223372036854775807:record-contribution:9999:v1")
        assertThat(RecordLikeRedisKey.recordLike(recordId = Long.MAX_VALUE, userId = 1L)).isEqualTo("record:9223372036854775807:like:1:v1")
    }

    @Test
    fun `다른 종류의 키는 서로 겹치지 않는다`() {
        // given: 같은 id를 쓰는 모든 키 종류
        val keys: List<String> = listOf(
            UserRedisKey.userRefreshToken(1L),
            RecordContributionRedisKey.userRecordContribution(1L, 2026),
            UserFollowRedisKey.userFollowerCount(1L),
            UserFollowRedisKey.userFollowingCount(1L),
            RecordLikeRedisKey.recordLikeCount(1L),
            RecordViewRedisKey.recordViewCount(1L),
            RecordLikeRedisKey.recordLike(1L, 1L),
            UserFollowRedisKey.userFollowRelation(1L, 1L),
            UserFollowRedisKey.userFollowLock(1L, 1L)
        )

        // when & then
        assertThat(keys).doesNotHaveDuplicates()
    }

    @Test
    fun `TTL 상수는 이전과 같은 값이다`() {
        assertThat(RecordLikeRedisKey.RECORD_LIKE_CACHE_TTL_MINUTES).isEqualTo(10L)
        assertThat(RecordContributionRedisKey.RECORD_CONTRIBUTION_CACHE_TTL_MINUTES).isEqualTo(5L)
    }
}
