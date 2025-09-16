package com.zzan.zzan.api.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    @GetMapping("/health")
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "timestamp" to System.currentTimeMillis(),
            "message" to "ZZAN API is running"
        )
    }

    @GetMapping("/")
    fun root(): Map<String, String> {
        return mapOf(
            "service" to "ZZAN API",
            "version" to "1.0.0"
        )
    }
}
