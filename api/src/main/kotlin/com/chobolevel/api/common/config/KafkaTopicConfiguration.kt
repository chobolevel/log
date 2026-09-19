package com.chobolevel.api.common.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfiguration {

    companion object {
        const val RECORD_LIKE_SYNC_EVENTS = "record-like-sync-events"
        const val RECORD_LIKE_SYNC_EVENTS_DLQ = "record-like-sync-events-dlq"
        const val RECORD_VIEW_EVENTS = "record-view-events"
        const val USER_FOLLOW_SYNC_EVENTS = "user-follow-sync-events"
        const val USER_FOLLOW_SYNC_EVENTS_DLQ = "user-follow-sync-events-dlq"
    }

    @Bean
    fun recordLikeSyncEventsTopic(): NewTopic {
        return TopicBuilder.name("record-like-sync-events")
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun recordLikeSyncEventsDlqTopic(): NewTopic {
        return TopicBuilder.name("record-like-sync-events-dlq")
            .partitions(1)
            .replicas(1)
            .build()
    }

    // 조회 이벤트는 순서 보장이 필요 없고(recordId 키 없이 라운드로빈 발행) 유실도 허용되므로
    // 좋아요와 달리 Outbox/DLQ 없이 이 토픽 하나로 처리한다.
    @Bean
    fun recordViewEventsTopic(): NewTopic {
        return TopicBuilder.name("record-view-events")
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun userFollowSyncEventsTopic(): NewTopic {
        return TopicBuilder.name("user-follow-sync-events")
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun userFollowSyncEventsDlqTopic(): NewTopic {
        return TopicBuilder.name("user-follow-sync-events-dlq")
            .partitions(1)
            .replicas(1)
            .build()
    }
}
