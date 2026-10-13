package com.chobolevel.api.common.transaction

import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

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
