package com.chobolevel.domain.common.exception

enum class ErrorCode(val defaultMessage: String) {
    // COMMON
    INVALID_PARAMETER("파라미터가 유효하지 않습니다."),
    INVALID_REQUEST_FORMAT("요청 형식이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR("내부 서버에서 에러가 발생하였습니다."),

    // AUTH
    BAD_CREDENTIAL("아이디 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("만료된 토큰입니다."),
    EMAIL_VERIFICATION_CODE_NOT_SENT("이메일 확인 코드가 전송되지 않았습니다."),
    EMAIL_VERIFICATION_CODE_NOT_MATCHED("이메일 확인 코드가 일치하지 않습니다."),
    ACCESS_DENIED("접근 권한이 없습니다."),
    BAD_CREDENTIALS("유효하지 않은 접근입니다."),

    // CHANNEL
    CHANNEL_NOT_FOUND("채널을 찾을 수 없습니다."),
    UNINVITED_CHANNEL("초대받지 않은 채널입니다."),
    ALREADY_EXITED_CHANNEL("이미 떠난 채널입니다."),
    ALREADY_INVITED_CHANNEL("이미 초대된 채널입니다."),
    RESTRICTED_TO_CHANNEL_OWNER("채널 오너만 접근 가능합니다."),

    // CHANNEL MESSAGE
    CHANNEL_MESSAGE_NOT_FOUND("채널 메시지를 찾을 수 없습니다."),
    CHANNEL_MESSAGE_WRITER_NOT_MATCHED("채널 메시지 작성자가 아닙니다."),

    // GUEST BOOK
    GUEST_BOOK_NOT_FOUND("방명록을 찾을 수 없습니다."),
    GUEST_BOOK_PASSWORD_NOT_MATCHED("방명록 비밀번호가 일치하지 않습니다."),

    // POST
    POST_NOT_FOUND("게시글을 찾을 수 없습니다."),
    RESTRICTED_TO_POST_WRITER("게시글 작성자만 접근 가능합니다."),

    // POST COMMENT
    POST_COMMENT_NOT_FOUND("게시글 댓글을 찾을 수 없습니다."),
    RESTRICTED_TO_POST_COMMENT_WRITER("게시글 댓글 작성자만 접근 가능합니다."),

    // TAG
    TAG_NOT_FOUND("태그를 찾을 수 없습니다."),

    // USER
    USER_NOT_FOUND("회원을 찾을 수 없습니다."),
    USER_PASSWORD_NOT_MATCHED("비밀번호가 일치하지 않습니다."),
    USER_PASSWORD_REUSING_NOT_ALLOWED("동일한 비밀번호를 사용할 수 없습니다."),
    USER_EMAIL_ALREADY_EXISTS("이미 존재하는 이메일입니다."),
    USER_NICKNAME_ALREADY_EXISTS("이미 존재하는 닉네임입니다."),

    // USER IMAGE
    USER_IMAGE_NOT_FOUND("회원 이미지를 찾을 수 없습니다.")
}
