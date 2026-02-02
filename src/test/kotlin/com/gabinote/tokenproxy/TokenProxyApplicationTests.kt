package com.gabinote.tokenproxy

import com.gabinote.tokenproxy.testSupport.testConfig.keycloak.UseTestKeycloak
import com.gabinote.tokenproxy.testSupport.testTemplate.IntegrationTestTemplate
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@UseTestKeycloak
@SpringBootTest
class TokenProxyApplicationTests{

    @Test
    fun contextLoads() {
    }

}
