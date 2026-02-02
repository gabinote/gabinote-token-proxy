package com.gabinote.tokenproxy.common.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope


@ConfigurationProperties(prefix = "keycloak.proxy")
open class TokenProperties (
    var allowedRedirectUri: String,
    var allowedIdpHints: List<String>
)