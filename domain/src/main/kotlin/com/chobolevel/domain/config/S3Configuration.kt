package com.chobolevel.domain.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3Configuration(
    @Value("\${cloud.aws.credentials.accessKey}")
    private val accessKey: String,
    @Value("\${cloud.aws.credentials.secretKey}")
    private val secretKey: String,
    @Value("\${cloud.aws.region.static}")
    private val region: String
) {

    @Bean
    fun amazonS3(): AmazonS3 {
        val awsCredentials = BasicAWSCredentials(accessKey, secretKey)
        val credentialProvider = AWSStaticCredentialsProvider(awsCredentials)
        return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(credentialProvider)
            .build()
    }
}
