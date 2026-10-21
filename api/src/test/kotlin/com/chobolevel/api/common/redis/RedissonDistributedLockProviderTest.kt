package com.chobolevel.api.common.redis

import com.chobolevel.api.common.container.RedisTestSupport
import com.chobolevel.domain.common.exception.InternalSystemException
import com.chobolevel.domain.common.exception.SystemErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

// 모듈 재편 5단계(docs/design/infrastructure-module.md, S-2): 이동 전에 분산 락 구현의 현재 계약을 실제 Redis 컨테이너로 고정한다.
// 시간에 의존하는 케이스는 판정 여유를 넉넉히 둔다.
@DisplayName("RedissonDistributedLockProvider 계약 테스트 (Redis 컨테이너)")
class RedissonDistributedLockProviderTest {

    private val lockProvider: DistributedLockProvider = RedissonDistributedLockProvider(RedisTestSupport.redisson)
    private val executor: ExecutorService = Executors.newFixedThreadPool(2)

    @AfterEach
    fun tearDown() {
        executor.shutdownNow()
    }

    private fun newKey(): String = "contract:lock:${UUID.randomUUID()}"

    @Test
    fun `락을 얻으면 action을 실행하고 그 결과를 반환한다`() {
        // given
        val key: String = newKey()

        // when
        val result: String = lockProvider.executeWithLock(key, 1, 5, TimeUnit.SECONDS) { "결과" }

        // then
        assertThat(result).isEqualTo("결과")
    }

    @Test
    fun `같은 키로 동시에 요청하면 한 번에 하나만 실행된다`() {
        // given
        val key: String = newKey()
        val inside: AtomicInteger = AtomicInteger(0)
        val maxInside: AtomicInteger = AtomicInteger(0)
        val executions: AtomicInteger = AtomicInteger(0)

        // when
        val futures: List<Future<Unit>> = (1..2).map {
            executor.submit<Unit> {
                lockProvider.executeWithLock(key, 5, 10, TimeUnit.SECONDS) {
                    maxInside.accumulateAndGet(inside.incrementAndGet()) { a, b -> maxOf(a, b) }
                    Thread.sleep(300)
                    inside.decrementAndGet()
                    executions.incrementAndGet()
                    Unit
                }
            }
        }
        futures.forEach { it.get(15, TimeUnit.SECONDS) }

        // then
        assertThat(executions.get()).isEqualTo(2)
        assertThat(maxInside.get()).isEqualTo(1)
    }

    @Test
    fun `서로 다른 키는 동시에 실행된다`() {
        // given: 두 스레드가 각자의 락 안에서 서로를 기다린다. 같은 락이었다면 배리어를 통과하지 못한다.
        val barrier: CyclicBarrier = CyclicBarrier(2)

        // when
        val futures: List<Future<Boolean>> = listOf(newKey(), newKey()).map { key ->
            executor.submit<Boolean> {
                lockProvider.executeWithLock(key, 1, 10, TimeUnit.SECONDS) {
                    barrier.await(3, TimeUnit.SECONDS)
                    true
                }
            }
        }

        // then
        futures.forEach { assertThat(it.get(10, TimeUnit.SECONDS)).isTrue }
    }

    @Test
    fun `대기 시간 안에 락을 얻지 못하면 LOCK_ACQUISITION_FAILED 예외가 발생한다`() {
        // given: 다른 스레드가 락을 쥔 채 놓지 않는다.
        val key: String = newKey()
        val entered: CountDownLatch = CountDownLatch(1)
        val release: CountDownLatch = CountDownLatch(1)
        val holder: Future<Unit> = executor.submit<Unit> {
            lockProvider.executeWithLock(key, 1, 20, TimeUnit.SECONDS) {
                entered.countDown()
                release.await(10, TimeUnit.SECONDS)
                Unit
            }
        }
        assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue

        // when
        val exception: InternalSystemException = assertThrows {
            lockProvider.executeWithLock(key, 200, 5000, TimeUnit.MILLISECONDS) { }
        }
        release.countDown()
        holder.get(10, TimeUnit.SECONDS)

        // then
        assertThat(exception.errorCode).isEqualTo(SystemErrorCode.LOCK_ACQUISITION_FAILED)
    }

    @Test
    fun `action이 예외를 던져도 락이 해제되어 다음 요청이 락을 얻는다`() {
        // given
        val key: String = newKey()

        // when
        assertThrows<IllegalStateException> {
            lockProvider.executeWithLock(key, 1, 20, TimeUnit.SECONDS) { throw IllegalStateException("실패") }
        }
        val result: String = lockProvider.executeWithLock(key, 0, 20, TimeUnit.SECONDS) { "다시 획득" }

        // then
        assertThat(result).isEqualTo("다시 획득")
    }

    @Test
    fun `leaseTime이 지나 락이 만료되면 다른 요청이 락을 얻고 기존 action은 예외 없이 끝난다`() {
        // given: 락을 쥔 action이 leaseTime(500ms)보다 오래(1500ms) 걸린다.
        val key: String = newKey()
        val entered: CountDownLatch = CountDownLatch(1)
        val holder: Future<String> = executor.submit<String> {
            lockProvider.executeWithLock(key, 1, 500, TimeUnit.MILLISECONDS) {
                entered.countDown()
                Thread.sleep(1500)
                "기존 action 완료"
            }
        }
        assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue
        Thread.sleep(900)

        // when: 만료된 뒤라 대기 없이도 락을 얻을 수 있다.
        val other: String = lockProvider.executeWithLock(key, 0, 20, TimeUnit.SECONDS) { "다른 요청이 획득" }

        // then: 기존 action은 이미 잃은 락을 해제하려다 실패하지 않고 정상 종료한다(isHeldByCurrentThread 확인).
        assertThat(other).isEqualTo("다른 요청이 획득")
        assertThat(holder.get(10, TimeUnit.SECONDS)).isEqualTo("기존 action 완료")
    }
}
