package com.gabinote.tokenproxy.common.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope


@ConfigurationProperties(prefix = "keycloak.admin-client")
data class KeycloakProperties(
    var serverUrl: String,
    var realm: String,
    var clientId: String,
    var clientSecret: String
)