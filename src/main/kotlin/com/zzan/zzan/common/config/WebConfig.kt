package com.zzan.zzan.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        val mapper = ObjectMapper().apply {
            registerKotlinModule() // Kotlin 지원을 위한 모듈 등록
            registerModule(JavaTimeModule()) // Java 8 Time API 지원을 위한 모듈 등록

            // 날짜를 ISO 문자열로 직렬화 (배열 형태 방지)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

            // 타입 정보를 JSON에 포함하지 않음 (클린한 API 응답)
            // activateDefaultTyping을 사용하지 않음
        }

        converters.add(0, MappingJackson2HttpMessageConverter(mapper))
    }
}
