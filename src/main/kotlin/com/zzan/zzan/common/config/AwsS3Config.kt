package com.zzan.zzan.common.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AwsS3Config {

    @Value("\${cloud.aws.credentials.access-key}")
    private lateinit var accessKey: String

    @Value("\${cloud.aws.credentials.secret-key}")
    private lateinit var secretKey: String

    @Value("\${cloud.aws.region.static}")
    private lateinit var region: String

    @Bean
    fun amazonS3(): AmazonS3 {
        val awsCredentials = BasicAWSCredentials(accessKey, secretKey)

        return AmazonS3ClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
            .withRegion(Regions.fromName(region))
            .build()
    }
}
