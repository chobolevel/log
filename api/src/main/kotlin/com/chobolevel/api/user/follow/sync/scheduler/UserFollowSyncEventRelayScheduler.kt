package com.chobolevel.api.user.follow.sync.scheduler

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.user.follow.sync.dto.UserFollowSyncEventMessage
import com.chobolevel.domain.user.follow.sync.entity.UserFollowSyncEvent
import com.chobolevel.domain.user.follow.sync.repository.UserFollowSyncEventRepository
import com.chobolevel.domain.user.follow.sync.vo.UserFollowSyncEventStatus
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class UserFollowSyncEventRelayScheduler(
    private val userFollowSyncEventRepository: UserFollowSyncEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, UserFollowSyncEventMessage>,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // [설계 의도: Transactional Outbox Relay]
    //
    // follow()/unfollow() 와 같은 트랜잭션에서 INSERT된 PENDING 이벤트를 Kafka로 중계한다.
    // 각 이벤트는 독립적으로 처리되며, Kafka 발행 성공 시에만 PUBLISHED로 업데이트한다.
    // 발행 실패 시 PENDING 상태가 유지되어 다음 실행 주기에 자동으로 재시도된다.
    //
    // 메시지 키는 "{followerUserId}:{followingUserId}" 쌍을 사용한다.
    // 파티션이 1개인 지금은 동작에 차이가 없지만, 파티션을 늘릴 때 같은 쌍의 이벤트가
    // 항상 같은 파티션으로 라우팅되어 순서(follow → unfollow → follow ...)가 보장된다.
    @Scheduled(fixedDelay = 5_000)
    fun relay() {
        val pendingEvents: List<UserFollowSyncEvent> = userFollowSyncEventRepository
            .findAllByStatus(UserFollowSyncEventStatus.PENDING)

        if (pendingEvents.isEmpty()) return

        logger.info("UserFollowSyncEvent relay 시작 - ${pendingEvents.size}개")

        pendingEvents.forEach { event ->
            runCatching {
                kafkaTemplate.send(
                    KafkaTopicConfiguration.USER_FOLLOW_SYNC_EVENTS,
                    "${event.followerUserId}:${event.followingUserId}",
                    UserFollowSyncEventMessage(
                        eventId = event.id!!,
                        followerUserId = event.followerUserId,
                        followingUserId = event.followingUserId,
                        action = event.action,
                    )
                ).get(5, TimeUnit.SECONDS)

                event.markPublished()
                userFollowSyncEventRepository.save(event)
            }.onFailure { e ->
                logger.error("UserFollowSyncEvent Kafka 발행 실패 - eventId: ${event.id}", e)
            }
        }

        logger.info("UserFollowSyncEvent relay 완료")
    }
}
