package com.chobolevel.api.common.constant

object CacheKeyPrefix {
    const val EMAIL = "user:email-verification:v1:"
    const val RESET_PASSWORD = "user:reset-password:v1:"
    const val REFRESH_TOKEN = "user:refresh-token:v1:"

    private const val RECORD_LIKES = "record:likes:v1:"
    const val RECORD_LIKES_DIRTY = "record:likes:v1:dirty"
    private const val RECORD_LIKES_LOCK = "lock:record:likes:v1:"

    fun recordLikes(recordId: Long): String = "$RECORD_LIKES$recordId"
    fun recordLikesLock(recordId: Long): String = "$RECORD_LIKES_LOCK$recordId"
}
