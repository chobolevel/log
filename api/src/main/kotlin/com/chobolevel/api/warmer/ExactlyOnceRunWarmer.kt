// package com.chobolevel.api.warmer
//
// import kotlinx.coroutines.sync.Mutex
//
// abstract class ExactlyOnceRunWarmer : Warmer {
//    override var isDone: Boolean = false
//
//    // 프로세스 내에서 여러 스레드의 임계 구역 제어를 위해 사용되는 객체
//    private val mutex = Mutex()
//
//    override suspend fun run() {
//        if (!isDone && mutex.tryLock()) {
//            try {
//                doRun()
//                setDone()
//            } finally {
//                mutex.unlock()
//            }
//        }
//    }
//
//    protected fun setDone() {
//        this.isDone = true
//    }
//
//    abstract suspend fun doRun()
// }
