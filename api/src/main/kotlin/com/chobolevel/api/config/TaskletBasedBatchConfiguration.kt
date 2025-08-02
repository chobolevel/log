package com.chobolevel.api.config

import org.joda.time.LocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.transaction.PlatformTransactionManager

// @Configuration
class TaskletBasedBatchConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {

    private final val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun taskletJob(): Job {
        return JobBuilder("taskletJob", jobRepository)
            .start(taskletStep())
            .build()
    }

    @Bean
    fun taskletStep(): Step {
        return StepBuilder("taskletStep", jobRepository)
            .tasklet(printHelloTasklet(), transactionManager)
            .build()
    }

    @Bean
    fun printHelloTasklet(): Tasklet {
        return Tasklet { contribution, chunkContext ->
            val now = LocalDateTime.now()
            logger.info("🟢 Tasklet 실행됨: Hello from Tasklet at $now")
            RepeatStatus.FINISHED
        }
    }
}
