package com.chobolevel.domain.exception

enum class ErrorCode {
    // common
    INVALID_PARAMETER,
    INVALID_FORMAT,

    // AUTH
    EXPIRED_TOKEN,
    INVALID_TOKEN,
    ACCESS_DENIED,
    BAD_CREDENTIALS,

    // UNKNOWN ERROR CODE
    UNKNOWN_ERROR,

    // AUTH
    A001, // email verification code not exists
    A002, // email verification code not match

    // USER
    USER_NOT_FOUND, // not found
    USER_EMAIL_ALREADY_EXISTS, // email already exists
    USER_NICKNAME_ALREADY_EXISTS, // nickname already exists
    USER_PASSWORD_NOT_MATCH, // password not match
    USER_PASSWORD_REUSE_NOT_ALLOWED, // password reuse not allowed

    // USER IMAGE
    UI001,

    // POST
    POST_ONLY_ACCESS_WRITER,

    // POST COMMENT
    POST_COMMENT_ONLY_ACCESS_WRITER,

    // channel
    CHANNEL_OWNER_DOES_NOT_MATCH,
    ALREADY_EXITED_CHANNEL,
    ALREADY_INVITED_THIS_CHANNEL,
    NOT_INVITED_CHANNEL,

    // channel message
    CHANNEL_MESSAGE_WRITER_DOES_NOT_MATCH,

    // LOG
    L001,
}
