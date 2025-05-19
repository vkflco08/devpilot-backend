package com.devpilot.backend.common.ratelimit

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Bucket4j
import io.github.bucket4j.Refill
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class RateLimitConfig {
    @Bean
    fun rateLimitBucket(): Bucket {
        // 1분당 최대 10회의 요청을 허용
        val limit = Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1)))
        return Bucket4j.builder().addLimit(limit).build()
    }
}
