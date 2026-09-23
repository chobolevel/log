package com.chobolevel.api.common.extension

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.Authentication
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.security.Principal
import java.time.OffsetDateTime

// TODO: 주석 작성 방법 확인해보기
@Deprecated(message = "Use Authentication.getUserId() instead of this.")
fun Principal.getUserId(): Long {
    return this.name.toLong()
}

fun Authentication.getUserId(): Long {
    return this.name.toLong()
}

fun HttpServletRequest.getCookie(key: String): String? {
    if (this.cookies.isNullOrEmpty() || this.cookies.find { it.name == key } == null) {
        return null
    }
    return this.cookies.find { it.name == key }!!.value
}

fun OffsetDateTime?.toMillis(): Long {
    return this?.toInstant()?.toEpochMilli() ?: 0L
}

// DB 커밋 성공 후에만 캐시 반영 등 부가 작업을 실행한다 — 트랜잭션 동기화가 없는 컨텍스트(테스트 등)에서는 즉시 실행한다.
fun registerAfterCommit(action: () -> Unit) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() = action()
        })
    } else {
        action()
    }
}
