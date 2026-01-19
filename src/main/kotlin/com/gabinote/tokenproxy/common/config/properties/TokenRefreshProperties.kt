package com.gabinote.tokenproxy.common.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "keycloak.proxy.refresh-cookie")
data class TokenRefreshProperties(
    val name: String,
    val maxAge: Long,
    val allowPath: String,
    val allowDomain: String
)