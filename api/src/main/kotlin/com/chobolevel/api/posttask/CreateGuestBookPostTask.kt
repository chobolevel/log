package com.chobolevel.api.posttask

import com.chobolevel.api.dto.discord.DiscordRequestDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Component
class CreateGuestBookPostTask(
    private val restTemplate: RestTemplate,
    @Value("\${logging.discord.webhook-url}") private val discordWebhookUrl: String,
    @Value("\${logging.discord.username}") private val discordUsername: String,
    @Value("\${logging.discord.avatar-url}") private val discordAvatarUrl: String,
) {

    @Async
    fun invoke() {
        val discordRequest = DiscordRequestDto(
            content = "\uD83D\uDCDD\t새로운 방명록이 등록되었습니다!\t\uD83D\uDCDD",
            username = discordUsername,
            avatarUrl = discordAvatarUrl,
            tts = false
        )
        restTemplate.postForEntity<String>(discordWebhookUrl, discordRequest)
    }
}
