package com.chobolevel.domain.common.kafka

// Outbox 이벤트가 오가는 Kafka 토픽 이름. 발행하는 쪽(relay 스케줄러)과 구독하는 쪽(consumer)이 같은 이름을 써야 하므로
// 두 곳이 모두 볼 수 있는 domain에 둔다. 이름이 바뀌면 이벤트가 유실되므로 문자열을 바꾸지 않는다.
// 토픽 생성(파티션·복제 설정)은 인프라 쪽 KafkaTopicConfiguration이 맡는다.
object KafkaTopics {
    const val RECORD_LIKE_SYNC_EVENTS = "record-like-sync-events"
    const val RECORD_LIKE_SYNC_EVENTS_DLQ = "record-like-sync-events-dlq"
    const val RECORD_VIEW_SYNC_EVENTS = "record-view-sync-events"
    const val RECORD_VIEW_SYNC_EVENTS_DLQ = "record-view-sync-events-dlq"
    const val USER_FOLLOW_SYNC_EVENTS = "user-follow-sync-events"
    const val USER_FOLLOW_SYNC_EVENTS_DLQ = "user-follow-sync-events-dlq"
    const val NOTIFICATION_DISPATCH_EVENTS = "notification-dispatch-events"
    const val NOTIFICATION_DISPATCH_EVENTS_DLQ = "notification-dispatch-events-dlq"
}
