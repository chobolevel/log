package com.chobolevel.api.scheduler

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled

// @Component
class ChunkBatchScheduler(
    private val jobLauncher: JobLauncher,
    private val chunkJob: Job
) {

    @Scheduled(cron = "0/10 * * * * *")
    fun runChunkJob() {
        val params = JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters()
        try {
            jobLauncher.run(chunkJob, params)
        } catch (e: Exception) {
            println("❌ Chunk Job 실행 실패: ${e.message}")
        }
    }
}
