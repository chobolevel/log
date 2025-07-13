package com.chobolevel.api.scheduler

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled

// @Component
class BatchScheduler(
    private val jobLauncher: JobLauncher,
    private val job: Job
) {

    @Scheduled(cron = "0/10 * * * * *")
    fun runTaskletJob() {
        val params = JobParametersBuilder()
            .addLong("run.id", System.currentTimeMillis())
            .toJobParameters()
        try {
            jobLauncher.run(job, params)
        } catch (e: Exception) {
            println("❌ Tasklet Job 실행 실패: ${e.message}")
        }
    }
}
