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
        const val RECORD_VIEW_SYNC_EVENTS = "record-view-sync-events"
        const val RECORD_VIEW_SYNC_EVENTS_DLQ = "record-view-sync-events-dlq"
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

    @Bean
    fun recordViewSyncEventsTopic(): NewTopic {
        return TopicBuilder.name("record-view-sync-events")
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun recordViewSyncEventsDlqTopic(): NewTopic {
        return TopicBuilder.name("record-view-sync-events-dlq")
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
