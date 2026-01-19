package com.gabinote.tokenproxy.common.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "keycloak.proxy")
data class TokenProperties (
    val allowedRedirectUri: String,
    val allowedIdpHints: List<String>
)