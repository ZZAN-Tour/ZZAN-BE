package com.zzan.zzan.common.annotation

import org.junit.jupiter.api.Tag
import org.springframework.test.context.TestPropertySource
import org.testcontainers.junit.jupiter.Testcontainers

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Tag("integration")
@Testcontainers
@TestPropertySource(locations = ["classpath:application-test.properties"])
annotation class IntegrationTest
