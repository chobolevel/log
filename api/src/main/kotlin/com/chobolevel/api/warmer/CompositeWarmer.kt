// package com.chobolevel.api.warmer
//
// import kotlinx.coroutines.NonCancellable
// import kotlinx.coroutines.async
// import kotlinx.coroutines.awaitAll
// import kotlinx.coroutines.withContext
// import org.springframework.context.annotation.Primary
// import org.springframework.stereotype.Component
//
// @Primary
// @Component
// class CompositeWarmer(
//    private val warmers: Collection<Warmer>
// ) : ExactlyOnceRunWarmer() {
//    override suspend fun doRun() {
//        // 이미 실행된 워머는 취소하지 않음
//        withContext(NonCancellable) {
//            warmers.map {
//                async { it.run() }
//            }.awaitAll()
//
//            if (warmers.all { it.isDone }) {
//                setDone()
//            }
//        }
//    }
// }
