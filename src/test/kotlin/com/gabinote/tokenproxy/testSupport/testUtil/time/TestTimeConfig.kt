package com.gabinote.tokenproxy.testSupport.testUtil.time

import com.gabinote.tokenproxy.common.util.time.TimeProvider
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class TestTimeConfig {

    @Bean
    fun timeProvider(): TimeProvider {
        return TestTimeProvider()
    }
}