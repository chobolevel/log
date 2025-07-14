package com.chobolevel.api.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.support.ListItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ChunkBasedBatchConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {

    private final val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun chunkJob(): Job {
        return JobBuilder("chunkJob", jobRepository)
            .start(chunkStep())
            .build()
    }

    @Bean
    fun chunkStep(): Step {
        return StepBuilder("chunkStep", jobRepository)
            .chunk<String, String>(3, transactionManager)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build()
    }

    @Bean
    fun itemReader(): ItemReader<String> {
        val data: List<String> = listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "ning", "ten")
        return ListItemReader(data)
    }

    @Bean
    fun itemProcessor(): ItemProcessor<String, String> {
        return ItemProcessor { item ->
            item.uppercase()
        }
    }

    @Bean
    fun itemWriter(): ItemWriter<String> {
        return ItemWriter { items ->
            logger.info("🔹 Chunk write: $items")
        }
    }
}
