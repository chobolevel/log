package com.chobolevel.api.notification.dispatch.scheduler

import com.chobolevel.api.common.config.KafkaTopicConfiguration
import com.chobolevel.api.notification.dispatch.dto.NotificationDispatchEventMessage
import com.chobolevel.domain.notification.dispatch.entity.NotificationDispatchEvent
import com.chobolevel.domain.notification.dispatch.repository.NotificationDispatchEventRepository
import com.chobolevel.domain.notification.dispatch.vo.NotificationDispatchEventStatus
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class NotificationDispatchEventRelayScheduler(
    private val notificationDispatchEventRepository: NotificationDispatchEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, NotificationDispatchEventMessage>,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val RELAY_CHUNK_SIZE = 500L
    }

    @Scheduled(fixedDelay = 5_000)
    fun relay() {
        val pendingEvents: List<NotificationDispatchEvent> = notificationDispatchEventRepository
            .findAllByStatusOrderByIdAsc(NotificationDispatchEventStatus.PENDING, RELAY_CHUNK_SIZE)

        if (pendingEvents.isEmpty()) return

        logger.info("NotificationDispatchEvent relay 시작 - ${pendingEvents.size}개")

        pendingEvents.forEach { event ->
            runCatching {
                kafkaTemplate.send(
                    KafkaTopicConfiguration.NOTIFICATION_DISPATCH_EVENTS,
                    event.userId.toString(),
                    NotificationDispatchEventMessage(
                        eventId = event.id!!,
                        userId = event.userId,
                        type = event.type,
                        content = event.content,
                        link = event.link,
                    )
                ).get(5, TimeUnit.SECONDS)

                event.markPublished()
                notificationDispatchEventRepository.save(event)
            }.onFailure { e ->
                logger.error("NotificationDispatchEvent Kafka 발행 실패 - eventId: ${event.id}", e)
            }
        }

        logger.info("NotificationDispatchEvent relay 완료")
    }
}
