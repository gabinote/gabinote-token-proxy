package com.gabinote.tokenproxy.common.config

import com.gabinote.tokenproxy.common.config.properties.TokenProperties
import com.gabinote.tokenproxy.common.config.properties.TokenRefreshProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(TokenProperties::class, TokenRefreshProperties::class)
@Configuration
class TokenConfig {
}