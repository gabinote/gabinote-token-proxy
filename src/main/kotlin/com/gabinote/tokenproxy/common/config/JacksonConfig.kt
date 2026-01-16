package com.gabinote.tokenproxy.common.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.format.DateTimeFormatter

/**
 * Jackson JSON 라이브러리 설정 클래스
 * JSON 변환 관련 설정 및 커스터마이징을 담당
 */
@Configuration
class JacksonConfig() {
    @Bean
    fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.serializers(LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            builder.deserializers(LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            builder.featuresToEnable(DeserializationFeature.USE_LONG_FOR_INTS)
        }
    }

}