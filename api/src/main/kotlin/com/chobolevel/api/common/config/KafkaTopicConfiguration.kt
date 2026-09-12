package com.chobolevel.api.common.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfiguration {

    companion object {
        const val RECORD_LIKE_SYNC_EVENTS = "record-like-sync-events"
        const val RECORD_LIKE_SYNC_EVENTS_RETRY = "record-like-sync-events-retry"
        const val RECORD_LIKE_SYNC_EVENTS_DLQ = "record-like-sync-events-dlq"
    }

    @Bean
    fun recordLikeSyncEventsTopic(): NewTopic {
        return TopicBuilder.name("record-like-sync-events")
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun recordLikeSyncEventsRetryTopic(): NewTopic {
        return TopicBuilder.name("record-like-sync-events-retry")
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
}
