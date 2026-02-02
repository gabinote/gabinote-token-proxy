package com.gabinote.tokenproxy.common.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope


@ConfigurationProperties(prefix = "keycloak.proxy.refresh-cookie")
data class TokenRefreshProperties(
    var name: String,
    var maxAge: Long,
    var allowPath: String,
    var allowDomain: String
)