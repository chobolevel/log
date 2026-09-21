package com.chobolevel.api.common.provider

import java.util.concurrent.TimeUnit

// 현재는 호출부(UserFollowFacade)에서 직접 호출 + try/finally로 해제하는 방식으로 사용한다.
// 락은 반드시 @Transactional 경계(UserFollowService) 바깥에서 감싸야 한다 — 안에서 감싸면
// 커밋 전에 락이 풀려 동시 요청이 같은 미커밋 상태를 보고 통과하는 문제가 생긴다.
// 이 락 패턴을 쓰는 곳이 여러 군데로 늘어나면, 그때 @DistributedLock 어노테이션 + AOP Aspect로
// (SpEL로 메서드 인자에서 키를 뽑아 @Transactional 바깥을 감싸도록) 승격하는 걸 고려한다.
interface DistributedLockProvider {

    // key에 대한 락을 획득한 뒤 action을 실행하고 락을 해제한다.
    // waitTime 안에 락을 획득하지 못하면 LOCK_ACQUISITION_FAILED 예외를 던진다.
    // leaseTime은 락을 쥔 스레드가 비정상 종료되어도 락이 영원히 남지 않도록 하는 안전장치다.
    fun <T> executeWithLock(
        key: String,
        waitTime: Long,
        leaseTime: Long,
        unit: TimeUnit,
        action: () -> T,
    ): T
}
