package com.chobolevel.domain.exception

enum class ErrorCode {
    // common
    INVALID_PARAMETER,

    // AUTH
    EXPIRED_TOKEN,
    INVALID_TOKEN,
    ACCESS_DENIED,
    BAD_CREDENTIALS,

    // UNKNOWN ERROR CODE
    UNKNOWN_ERROR,

    // USER
    // NOT FOUND
    U001,

    // USER IMAGE
    UI001,

    // channel
    CHANNEL_OWNER_DOES_NOT_MATCH
}
