package com.gabinote.coffeenote.testSupport.testConfig.jackson

import com.gabinote.tokenproxy.common.config.JacksonConfig
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited


@Import(
    JacksonConfig::class,
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class UseJackson
