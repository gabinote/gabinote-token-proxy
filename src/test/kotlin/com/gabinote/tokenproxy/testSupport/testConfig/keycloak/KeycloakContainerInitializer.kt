package com.gabinote.tokenproxy.testSupport.testConfig.keycloak

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

private val logger = KotlinLogging.logger {}

class KeycloakContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {

        const val REALM_IMPORT_FILE = "testsets/keycloak/realm-export.json"

        const val KEYCLOAK_ADMIN_USERNAME = "admin"
        const val KEYCLOAK_ADMIN_PASSWORD = "admin"

        @JvmStatic
        val keycloak: KeycloakContainer = KeycloakContainer("quay.io/keycloak/keycloak:latest")
            .withRealmImportFile(REALM_IMPORT_FILE)
            .withLabel("test-container", "keycloak")
            .withAdminPassword(KEYCLOAK_ADMIN_PASSWORD)
            .withAdminUsername(KEYCLOAK_ADMIN_USERNAME)
            .withReuse(true)
    }


    override fun initialize(context: ConfigurableApplicationContext) {
        // 테스트 컨테이너 시작
        keycloak.start()
        logger.debug { "Keycloak started on port ${keycloak.httpPort},url ${keycloak.authServerUrl}" }
        TestPropertyValues.of(
            "keycloak.admin-client.server-url=${keycloak.authServerUrl}",
            "keycloak.admin-client.realm=gabinote-test",
            "keycloak.admin-client.client-id=gabinote-test-client",
            "keycloak.admin-client.client-secret=test-secret",

        ).applyTo(context.environment)


    }


}