package com.chobolevel.api.common.kafka

import com.chobolevel.domain.common.kafka.KafkaTopics
import com.chobolevel.infrastructure.kafka.KafkaTopicConfiguration
import org.apache.kafka.clients.admin.NewTopic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-5): 토픽 이름 상수를 domain으로 옮기기 전에 이름을 고정한다.
// 토픽 이름이 바뀌면 relay가 발행하는 곳과 consumer가 구독하는 곳이 어긋나 Outbox 이벤트가 유실된다.
@DisplayName("Kafka 토픽 이름 고정 테스트")
class KafkaTopicConfigurationTest {

    private val configuration: KafkaTopicConfiguration = KafkaTopicConfiguration()

    @Test
    fun `토픽 이름 상수는 이전과 같은 문자열이다`() {
        assertThat(KafkaTopics.RECORD_LIKE_SYNC_EVENTS).isEqualTo("record-like-sync-events")
        assertThat(KafkaTopics.RECORD_LIKE_SYNC_EVENTS_DLQ).isEqualTo("record-like-sync-events-dlq")
        assertThat(KafkaTopics.RECORD_VIEW_SYNC_EVENTS).isEqualTo("record-view-sync-events")
        assertThat(KafkaTopics.RECORD_VIEW_SYNC_EVENTS_DLQ).isEqualTo("record-view-sync-events-dlq")
        assertThat(KafkaTopics.USER_FOLLOW_SYNC_EVENTS).isEqualTo("user-follow-sync-events")
        assertThat(KafkaTopics.USER_FOLLOW_SYNC_EVENTS_DLQ).isEqualTo("user-follow-sync-events-dlq")
        assertThat(KafkaTopics.NOTIFICATION_DISPATCH_EVENTS).isEqualTo("notification-dispatch-events")
        assertThat(KafkaTopics.NOTIFICATION_DISPATCH_EVENTS_DLQ).isEqualTo("notification-dispatch-events-dlq")
    }

    @Test
    fun `토픽 빈은 이전과 같은 이름, 파티션 1개, 복제 1개로 만들어진다`() {
        // given
        val topics: Map<String, NewTopic> = mapOf(
            "record-like-sync-events" to configuration.recordLikeSyncEventsTopic(),
            "record-like-sync-events-dlq" to configuration.recordLikeSyncEventsDlqTopic(),
            "record-view-sync-events" to configuration.recordViewSyncEventsTopic(),
            "record-view-sync-events-dlq" to configuration.recordViewSyncEventsDlqTopic(),
            "user-follow-sync-events" to configuration.userFollowSyncEventsTopic(),
            "user-follow-sync-events-dlq" to configuration.userFollowSyncEventsDlqTopic(),
            "notification-dispatch-events" to configuration.notificationDispatchEventsTopic(),
            "notification-dispatch-events-dlq" to configuration.notificationDispatchEventsDlqTopic()
        )

        // when & then
        assertThat(topics).hasSize(8)
        topics.forEach { (expectedName, topic) ->
            assertThat(topic.name()).describedAs("토픽 빈 이름").isEqualTo(expectedName)
            assertThat(topic.numPartitions()).describedAs("${topic.name()} 파티션 수").isEqualTo(1)
            assertThat(topic.replicationFactor().toInt()).describedAs("${topic.name()} 복제 수").isEqualTo(1)
        }
    }

    @Test
    fun `DLQ 토픽 이름은 원본 토픽 이름에 -dlq를 붙인 형태다`() {
        assertThat(KafkaTopics.RECORD_LIKE_SYNC_EVENTS_DLQ).isEqualTo("${KafkaTopics.RECORD_LIKE_SYNC_EVENTS}-dlq")
        assertThat(KafkaTopics.RECORD_VIEW_SYNC_EVENTS_DLQ).isEqualTo("${KafkaTopics.RECORD_VIEW_SYNC_EVENTS}-dlq")
        assertThat(KafkaTopics.USER_FOLLOW_SYNC_EVENTS_DLQ).isEqualTo("${KafkaTopics.USER_FOLLOW_SYNC_EVENTS}-dlq")
        assertThat(KafkaTopics.NOTIFICATION_DISPATCH_EVENTS_DLQ).isEqualTo("${KafkaTopics.NOTIFICATION_DISPATCH_EVENTS}-dlq")
    }
}
