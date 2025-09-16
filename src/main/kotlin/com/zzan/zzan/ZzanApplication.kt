package com.zzan.zzan

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync


@EnableCaching
@EnableJpaAuditing
@EnableAsync // 추가
@SpringBootApplication(
    exclude = [SecurityAutoConfiguration::class, UserDetailsServiceAutoConfiguration::class],
    scanBasePackages = ["com.zzan.zzan"]
)
class ZzanApplication

fun main(args: Array<String>) {
    runApplication<ZzanApplication>(*args)
}
