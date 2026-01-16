package com.gabinote.tokenproxy.common.config

import com.gabinote.tokenproxy.common.util.time.DefaultTimeProvider
import com.gabinote.tokenproxy.common.util.time.TimeProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Clock

/**
 * 시간 관련 설정 클래스
 * 애플리케이션 전체에서 사용할 시간 관련 빈을 구성
 */
@Profile("!test")
@Configuration
class TimeConfig {

    /**
     * 시스템 기본 시간대의 시계
     */
    private val clock: Clock = Clock.systemDefaultZone()

    /**
     * TimeProvider 빈 구성
     * 애플리케이션 전체에서 일관된 시간 제공을 위한 유틸리티
     * @return TimeProvider 인스턴스
     */
    @Bean
    fun timeHelper(): TimeProvider {
        return DefaultTimeProvider(clock)
    }

}