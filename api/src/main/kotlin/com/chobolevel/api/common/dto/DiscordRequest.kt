package com.chobolevel.api.common.dto

data class DiscordRequest(
    val content: String,
    val username: String,
    val avatarUrl: String,
    val tts: Boolean
)
