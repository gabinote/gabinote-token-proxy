package com.gabinote.tokenproxy.testSupport.testUtil.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.gabinote.tokenproxy.testSupport.testConfig.keycloak.KeycloakContainerInitializer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.RealmRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestComponent
import org.springframework.core.io.ClassPathResource
import org.springframework.web.client.RestTemplate


private val logger = KotlinLogging.logger {}

@TestComponent
class TestKeycloakUtil(
    private val restTemplate: RestTemplate = RestTemplate(),
    private val keycloak: Keycloak
) {


    @Value("\${keycloak.admin-client.realm}")
    lateinit var realm: String

    @Value("\${keycloak.admin-client.server-url}")
    lateinit var serverUrl: String


    val adminKeycloak: Keycloak by lazy {
        KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm("master")
            .clientId("admin-cli")
            .username(KeycloakContainerInitializer.Companion.KEYCLOAK_ADMIN_USERNAME)
            .password(KeycloakContainerInitializer.Companion.KEYCLOAK_ADMIN_PASSWORD)
            .build()
    }


    val mapper = ObjectMapper().registerModule(kotlinModule())



    fun recreateRealm() {
        try {
            adminKeycloak.realms().realm(realm).remove()
            logger.info { "Realm $realm deleted successfully." }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to delete realm $realm, it may not exist." }
        }
        try {
            val resource = ClassPathResource(KeycloakContainerInitializer.Companion.REALM_IMPORT_FILE)
            val inputStream = resource.inputStream

            val realm: RealmRepresentation = mapper.readValue(inputStream)

            adminKeycloak.realms().create(realm)
        } catch (e: Exception) {
            logger.error(e) { "Failed to create realm $realm." }
            throw e
        }

        // Realm 재생성 후 Service Account의 토큰을 강제로 갱신
        refreshServiceAccountToken()
    }

    /**
     * Realm 재생성 후 메인 애플리케이션의 Keycloak 빈 토큰을 갱신합니다.
     * Realm이 삭제되면 기존 토큰이 무효화되므로 새 토큰을 발급받아야 합니다.
     */
    private fun refreshServiceAccountToken() {
        try {
            val tokenManager = keycloak.tokenManager()
            // 캐시된 토큰을 무효화
            val currentToken = tokenManager.accessTokenString
            if (currentToken != null) {
                tokenManager.invalidate(currentToken)
            }
            // 새 토큰 발급
            tokenManager.grantToken()
            logger.info { "Service account token refreshed successfully after realm recreation." }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to refresh service account token, will be refreshed on next request." }
        }
    }





    fun getUser(sub: String): UserRepresentation {
        try {
            val user = keycloak.realm(realm).users().get(sub).toRepresentation()
            logger.info { "User $sub retrieved successfully: $user" }
            return user
        } catch (e: Exception) {
            logger.error(e) { "Failed to retrieve user $sub." }
            throw e
        }
    }

    fun validationUserGroup(
        sub: String,
        groupName: String,
    ): Boolean {
        try {
            val user = getUser(sub)
            val groups = keycloak.realm(realm).users().get(user.id).groups()
            return groups.any { it.name == groupName }
        } catch (e: Exception) {
            logger.error(e) { "Failed to validate user group for $sub." }
            throw e
        }
    }

    fun validationUserRole(
        sub: String,
        roleName: String,
    ): Boolean {
        try {
            val user = getUser(sub)
            val roles = keycloak.realm(realm).users().get(user.id).roles().realmLevel().listAll()
            return roles.any { it.name == roleName }
        } catch (e: Exception) {
            logger.error(e) { "Failed to validate user role for $sub." }
            throw e
        }
    }

    fun validationUserExist(
        sub: String,
        negativeMode: Boolean = false
    ): Boolean {
        return try {
            getUser(sub)
            if (negativeMode) {
                logger.info { "User $sub exists, but negative mode is enabled." }
                false
            } else {
                logger.info { "User $sub exists." }
                true
            }
        } catch (e: Exception) {
            if (negativeMode) {
                logger.info { "User $sub does not exist, as expected in negative mode." }
                true
            } else {
                logger.error(e) { "Failed to validate existence of user $sub." }
                false
            }
        }
    }

    fun validationUserEnabled(
        sub: String,
        reverseMode: Boolean = false
    ): Boolean {
        try {
            val user = getUser(sub)
            return if (reverseMode) {
                logger.info { "User $sub enabled status is ${user.isEnabled}, reverse mode is enabled." }
                !user.isEnabled
            } else {
                logger.info { "User $sub enabled status is ${user.isEnabled}." }
                user.isEnabled
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to validate enabled status of user $sub." }
            throw e
        }
    }

}