package com.gabinote.tokenproxy.testSupport.testTemplate

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabinote.tokenproxy.testSupport.testConfig.keycloak.UseTestKeycloak
import com.gabinote.tokenproxy.testSupport.testUtil.time.TestTimeProvider
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.core.test.TestCaseOrder
import io.restassured.RestAssured
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.junit.jupiter.Testcontainers


@UseTestKeycloak
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
@Import(
    TestTimeProvider::class,
)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class IntegrationTestTemplate : FeatureSpec() {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var objectMapper: ObjectMapper



    val apiPrefix: String = "/api/v1"


    fun beforeSpec() {
        RestAssured.basePath = apiPrefix
        RestAssured.port = port
    }

    override fun testCaseOrder(): TestCaseOrder = TestCaseOrder.Random

    init {
        beforeSpec {
            beforeSpec()
        }
    }
}