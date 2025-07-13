package com.chobolevel.api.config

import org.joda.time.LocalDateTime
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BatchConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    fun exampleJob(): Job {
        return JobBuilder("exampleTaskletJob", jobRepository)
            .start(exampleStep())
            .build()
    }

    @Bean
    fun exampleStep(): Step {
        return StepBuilder("exampleTaskletStep", jobRepository)
            .tasklet(printHelloTasklet(), transactionManager)
            .build()
    }

    @Bean
    fun printHelloTasklet(): Tasklet {
        return Tasklet { contribution, chunkContext ->
            val now = LocalDateTime.now()
            println("🟢 Tasklet 실행됨: Hello from Tasklet at $now")
            RepeatStatus.FINISHED
        }
    }
}
