package com.gabinote.tokenproxy.testSupport.testUtil.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.gabinote.tokenproxy.common.config.properties.KeycloakProperties
import com.gabinote.tokenproxy.testSupport.testConfig.keycloak.KeycloakContainerInitializer
import io.github.oshai.kotlinlogging.KotlinLogging
import io.quarkus.deployment.dev.testing.TestConfig
import io.restassured.RestAssured
import io.restassured.response.Response
import org.codehaus.groovy.classgen.Verifier
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.RealmRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestComponent
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate


private val logger = KotlinLogging.logger {}

@TestComponent
class TestKeycloakUtil(
    private val restTemplate: RestTemplate = RestTemplate(),
    private val keycloakProperties: KeycloakProperties,
) {


    @Value("\${keycloak.admin-client.realm}")
    lateinit var realm: String

    @Value("\${keycloak.admin-client.server-url}")
    lateinit var serverUrl: String

    val testKeycloak: Keycloak by lazy {
        KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .realm("gabinote-test")
            .clientId("api-admin-client")
            .clientSecret("admin-client-secret")
            .build()
    }
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
            val resource = ClassPathResource(KeycloakContainerInitializer.REALM_IMPORT_FILE)
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
            val tokenManager = testKeycloak.tokenManager()
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

    fun getTokens(testUser: TestUser): TestTokenRes {
        if (testUser == TestUser.INVALID) throw IllegalArgumentException("Invalid user")
        val tokenUrl = "$serverUrl/realms/$realm/protocol/openid-connect/token"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            add("Accept", MediaType.APPLICATION_JSON_VALUE)
        }
        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "password")
            add("client_id", keycloakProperties.clientId)
            add("client_secret", keycloakProperties.clientSecret)
            add("username", testUser.id)
            add("password", testUser.password)
            add("scope", "openid email profile")
        }
        val response = restTemplate.postForEntity(
            tokenUrl,
            HttpEntity(body, headers),
            String::class.java
        )
        if (response.statusCode == org.springframework.http.HttpStatus.OK) {
            val json = org.json.JSONObject(response.body)
            return TestTokenRes(
                accessToken = json.getString("access_token"),
                refreshToken = json.getString("refresh_token")
            )
        } else {
            throw kotlin.RuntimeException("Failed to get access token: ${response.body}")
        }
    }




    fun getUser(sub: String): UserRepresentation {
        try {
            val user =testKeycloak.realm(realm).users().get(sub).toRepresentation()
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
            val groups = testKeycloak.realm(realm).users().get(user.id).groups()
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
            val roles = testKeycloak.realm(realm).users().get(user.id).roles().realmLevel().listAll()
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
    fun validationAccessTokenValid(accessToken:String):Boolean{
        val introspectUrl = "${keycloakProperties.serverUrl}/realms/${keycloakProperties.realm}/protocol/openid-connect/token/introspect"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            setBasicAuth(keycloakProperties.clientId, keycloakProperties.clientSecret)
        }

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("token", accessToken)
        }

        val requestEntity = HttpEntity(formData, headers)

        val response = restTemplate.postForEntity(introspectUrl, requestEntity, Map::class.java)

        val responseBody = response.body as Map<String, Any>

        return responseBody["active"] as? Boolean ?: false
    }

    fun validationRefreshTokenValid(refreshToken:String):Boolean{
        // Keycloak에서는 리프레시 토큰의 유효성을 직접 확인하는 API를 제공하지 않음
        // 따라서 리프레시 토큰을 사용하여 액세스 토큰을 갱신 시도 후 성공 여부로 판단
        val tokenUrl = "${keycloakProperties.serverUrl}/realms/${keycloakProperties.realm}/protocol/openid-connect/token"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            setBasicAuth(keycloakProperties.clientId, keycloakProperties.clientSecret)
        }

        val formData = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "refresh_token")
            add("refresh_token", refreshToken)
        }

        val requestEntity = HttpEntity(formData, headers)

        return try {
            val response = restTemplate.postForEntity(tokenUrl, requestEntity, Map::class.java)
            response.statusCode.is2xxSuccessful
        } catch (e: Exception) {
            false
        }
    }
    fun getAuthCode(redirectUrl: String, username: String, password: String,challenge: String): String {
        val authUrl = "${keycloakProperties.serverUrl}/realms/${keycloakProperties.realm}/protocol/openid-connect/auth"

        // 1. 로그인 페이지 접근 (Cookies 획득)
        val loginPageResponse: Response = RestAssured.given()
            .redirects().follow(false)
            .queryParam("response_type", "code")
            .queryParam("client_id", "gabinote-test-client")
            .queryParam("scope", "openid")
            .queryParam("redirect_uri", redirectUrl)
            .queryParam("code_challenge", challenge)
            .queryParam("code_challenge_method", "S256")
            .get(authUrl)

        // HTML에서 action URL 추출
        val htmlBody = loginPageResponse.body.asString()
        val actionUrlMatch = Regex("action=\"([^\"]*)\"").find(htmlBody)
        val actionUrl = actionUrlMatch?.groupValues?.get(1)?.replace("&amp;", "&")
            ?: throw RuntimeException("Cannot find login form action url")

        // 2. ID/PW 전송 (로그인 수행)
        val loginResponse: Response = RestAssured.given()
            .redirects().follow(false)
            .cookies(loginPageResponse.cookies)
            .contentType("application/x-www-form-urlencoded")
            .formParam("username", username)
            .formParam("password", password)
            .post(actionUrl)
        val body = loginResponse.body.asString()
        // 3. 리다이렉트 된 URL에서 'code' 파라미터 추출
        val location = loginResponse.header("Location")
            ?: throw RuntimeException("No redirect location found. Status: ${loginResponse.statusCode}")

        val finalUrl = if (loginResponse.statusCode in 300..399) location else {
            throw RuntimeException("Login failed with status: ${loginResponse.statusCode}")
        }

        // URL에서 code=... 추출
        val codeMatch = Regex("code=([^&]*)").find(finalUrl)
        val res = codeMatch?.groupValues?.get(1)
            ?: throw RuntimeException("Failed to retrieve auth code from URL: $finalUrl")

        return res
    }
}