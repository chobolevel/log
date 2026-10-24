package com.chobolevel.api.common.kafka

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
        assertThat(KafkaTopicConfiguration.RECORD_LIKE_SYNC_EVENTS).isEqualTo("record-like-sync-events")
        assertThat(KafkaTopicConfiguration.RECORD_LIKE_SYNC_EVENTS_DLQ).isEqualTo("record-like-sync-events-dlq")
        assertThat(KafkaTopicConfiguration.RECORD_VIEW_SYNC_EVENTS).isEqualTo("record-view-sync-events")
        assertThat(KafkaTopicConfiguration.RECORD_VIEW_SYNC_EVENTS_DLQ).isEqualTo("record-view-sync-events-dlq")
        assertThat(KafkaTopicConfiguration.USER_FOLLOW_SYNC_EVENTS).isEqualTo("user-follow-sync-events")
        assertThat(KafkaTopicConfiguration.USER_FOLLOW_SYNC_EVENTS_DLQ).isEqualTo("user-follow-sync-events-dlq")
        assertThat(KafkaTopicConfiguration.NOTIFICATION_DISPATCH_EVENTS).isEqualTo("notification-dispatch-events")
        assertThat(KafkaTopicConfiguration.NOTIFICATION_DISPATCH_EVENTS_DLQ).isEqualTo("notification-dispatch-events-dlq")
    }

    @Test
    fun `토픽 빈은 상수와 같은 이름, 파티션 1개, 복제 1개로 만들어진다`() {
        // given
        val topics: Map<String, NewTopic> = mapOf(
            KafkaTopicConfiguration.RECORD_LIKE_SYNC_EVENTS to configuration.recordLikeSyncEventsTopic(),
            KafkaTopicConfiguration.RECORD_LIKE_SYNC_EVENTS_DLQ to configuration.recordLikeSyncEventsDlqTopic(),
            KafkaTopicConfiguration.RECORD_VIEW_SYNC_EVENTS to configuration.recordViewSyncEventsTopic(),
            KafkaTopicConfiguration.RECORD_VIEW_SYNC_EVENTS_DLQ to configuration.recordViewSyncEventsDlqTopic(),
            KafkaTopicConfiguration.USER_FOLLOW_SYNC_EVENTS to configuration.userFollowSyncEventsTopic(),
            KafkaTopicConfiguration.USER_FOLLOW_SYNC_EVENTS_DLQ to configuration.userFollowSyncEventsDlqTopic(),
            KafkaTopicConfiguration.NOTIFICATION_DISPATCH_EVENTS to configuration.notificationDispatchEventsTopic(),
            KafkaTopicConfiguration.NOTIFICATION_DISPATCH_EVENTS_DLQ to configuration.notificationDispatchEventsDlqTopic()
        )

        // when & then
        assertThat(topics).hasSize(8)
        topics.forEach { (constantName, topic) ->
            assertThat(topic.name()).describedAs("토픽 빈 이름").isEqualTo(constantName)
            assertThat(topic.numPartitions()).describedAs("${topic.name()} 파티션 수").isEqualTo(1)
            assertThat(topic.replicationFactor().toInt()).describedAs("${topic.name()} 복제 수").isEqualTo(1)
        }
    }

    @Test
    fun `DLQ 토픽 이름은 원본 토픽 이름에 -dlq를 붙인 형태다`() {
        assertThat(KafkaTopicConfiguration.RECORD_LIKE_SYNC_EVENTS_DLQ).isEqualTo("${KafkaTopicConfiguration.RECORD_LIKE_SYNC_EVENTS}-dlq")
        assertThat(KafkaTopicConfiguration.RECORD_VIEW_SYNC_EVENTS_DLQ).isEqualTo("${KafkaTopicConfiguration.RECORD_VIEW_SYNC_EVENTS}-dlq")
        assertThat(KafkaTopicConfiguration.USER_FOLLOW_SYNC_EVENTS_DLQ).isEqualTo("${KafkaTopicConfiguration.USER_FOLLOW_SYNC_EVENTS}-dlq")
        assertThat(KafkaTopicConfiguration.NOTIFICATION_DISPATCH_EVENTS_DLQ).isEqualTo("${KafkaTopicConfiguration.NOTIFICATION_DISPATCH_EVENTS}-dlq")
    }
}
