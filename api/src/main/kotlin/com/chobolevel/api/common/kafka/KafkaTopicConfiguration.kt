package com.chobolevel.api.common.kafka

import com.chobolevel.domain.common.kafka.KafkaTopics
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfiguration {

    @Bean
    fun recordLikeSyncEventsTopic(): NewTopic {
        return TopicBuilder.name(KafkaTopics.RECORD_LIKE_SYNC_EVENTS)
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun recordLikeSyncEventsDlqTopic(): NewTopic {
        return TopicBuilder.name(KafkaTopics.RECORD_LIKE_SYNC_EVENTS_DLQ)
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun recordViewSyncEventsTopic(): NewTopic {
        return TopicBuilder.name(KafkaTopics.RECORD_VIEW_SYNC_EVENTS)
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun recordViewSyncEventsDlqTopic(): NewTopic {
        return TopicBuilder.name(KafkaTopics.RECORD_VIEW_SYNC_EVENTS_DLQ)
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun userFollowSyncEventsTopic(): NewTopic {
        return TopicBuilder.name(KafkaTopics.USER_FOLLOW_SYNC_EVENTS)
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun userFollowSyncEventsDlqTopic(): NewTopic {
        return TopicBuilder.name(KafkaTopics.USER_FOLLOW_SYNC_EVENTS_DLQ)
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun notificationDispatchEventsTopic(): NewTopic {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_DISPATCH_EVENTS)
            .partitions(1)
            .replicas(1)
            .build()
    }

    @Bean
    fun notificationDispatchEventsDlqTopic(): NewTopic {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_DISPATCH_EVENTS_DLQ)
            .partitions(1)
            .replicas(1)
            .build()
    }
}
