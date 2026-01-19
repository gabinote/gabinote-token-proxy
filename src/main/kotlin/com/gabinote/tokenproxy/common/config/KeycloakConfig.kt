package com.gabinote.tokenproxy.common.config

import com.gabinote.tokenproxy.common.config.properties.KeycloakProperties
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.authorization.client.AuthzClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.keycloak.authorization.client.Configuration as KeycloakAuthzConfiguration

@EnableConfigurationProperties(KeycloakProperties::class)
@Configuration
class KeycloakConfig(
    private val props: KeycloakProperties
) {

    /*
   *  Keycloak 서버와 통신하기 위한 클라이언트 빌더
   * */
    @Bean
    fun keycloak(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl(props.serverUrl)
            .realm(props.realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(props.clientId)
            .clientSecret(props.clientSecret)
            .build()
    }

}