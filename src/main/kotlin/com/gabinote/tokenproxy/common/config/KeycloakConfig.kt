package com.gabinote.tokenproxy.common.config

import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KeycloakConfig {

    @Value("\${keycloak.admin-client.server-url}")
    lateinit var authServerUrl: String

    @Value("\${keycloak.admin-client.realm}")
    lateinit var realm: String

    @Value("\${keycloak.admin-client.client-id}")
    lateinit var clientId: String

    @Value("\${keycloak.admin-client.client-secret}")
    lateinit var clientSecret: String

    /*
   *  Keycloak 서버와 통신하기 위한 클라이언트 빌더
   * */
    @Bean
    fun keycloak(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(authServerUrl)
            .realm(realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build()
    }
}