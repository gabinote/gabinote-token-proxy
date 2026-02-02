package com.gabinote.tokenproxy.keycloak.auth.provider

import com.gabinote.tokenproxy.common.config.properties.KeycloakProperties
import com.gabinote.tokenproxy.common.util.exception.service.BadToken
import com.gabinote.tokenproxy.common.util.exception.service.ServerError
import com.gabinote.tokenproxy.testSupport.testTemplate.IntegrationTestTemplate
import com.gabinote.tokenproxy.testSupport.testUtil.keycloak.PkceHelper
import com.gabinote.tokenproxy.testSupport.testUtil.keycloak.TestUser
import com.gabinote.tokenproxy.token.dto.token.service.RedirectIdpReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeReqServiceDto
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.util.UriComponentsBuilder

class KeycloakTokenProviderTest : IntegrationTestTemplate() {

    @Autowired
    lateinit var keycloakTokenProvider: KeycloakTokenProvider

    @Autowired
    lateinit var prop: KeycloakProperties

    init {

        feature("[Keycloak] KeycloakTokenProvider Test"){

            feature("KeycloakTokenProvider.generateIdpUri"){
                scenario("정상적인 RedirectIdpReqServiceDto 가 주어지면, IdP 인증 URI 를 생성한다.") {
                    val redirectUri = "localhost/callback"
                    val verifier = PkceHelper.generateCodeVerifier()
                    val challenge = PkceHelper.generateCodeChallenge(verifier)
                    val idpHint = "idp_hint"
                    val req = RedirectIdpReqServiceDto(
                        redirectUri = redirectUri,
                        codeChallenge = challenge,
                        idpHint = idpHint
                    )

                    val idpUri = keycloakTokenProvider.generateIdpUri(req)

                    idpUri shouldBe "${prop.serverUrl}/realms/gabinote-test/protocol/openid-connect/auth?client_id=gabinote-test-client&redirect_uri=${redirectUri}&response_type=code&scope=openid&code_challenge=${challenge}&code_challenge_method=S256&kc_idp_hint=${idpHint}"
                }
            }

            feature("KeycloakTokenProvider.exchangeToken"){
                scenario("정상적인 Authorization Code 가 주어지면, Access Token 과 Refresh Token 을 발급한다.") {
                    val testUser = TestUser.USER
                    val verifier = PkceHelper.generateCodeVerifier()
                    val challenge = PkceHelper.generateCodeChallenge(verifier)
                    val redirect = "localhost/callback"
                    val authToken = testKeycloakUtil.getAuthCode(redirect,testUser.id, testUser.password,challenge)
                    val req = TokenExchangeReqServiceDto(
                        token = authToken,
                        redirectUri = redirect,
                        verifier = verifier
                    )
                    val res = keycloakTokenProvider.exchangeToken(req)

                    testKeycloakUtil.validationAccessTokenValid(res.accessToken) shouldBe true
                    testKeycloakUtil.validationRefreshTokenValid(res.refreshToken) shouldBe true
                }
                scenario("잘못된 Authorization Code 가 주어지면, 예외를 던진다.") {
                    val verifier = PkceHelper.generateCodeVerifier()
                    val redirect = "localhost/callback"
                    val req = TokenExchangeReqServiceDto(
                        token = "invalid authorization code",
                        redirectUri = redirect,
                        verifier = verifier
                    )

                    val ex = assertThrows<BadToken> {
                        keycloakTokenProvider.exchangeToken(req)
                    }
                }
            }

            feature("KeycloakTokenProvider.refreshToken"){
                scenario("정상적인 Refresh Token 이 주어지면, 새로운 Access Token 과 Refresh Token 을 발급한다.") {
                    val testUser = TestUser.USER
                    val verifier = PkceHelper.generateCodeVerifier()
                    val challenge = PkceHelper.generateCodeChallenge(verifier)
                    val redirect = "localhost/callback"
                    val authToken = testKeycloakUtil.getAuthCode(redirect,testUser.id, testUser.password,challenge)
                    val exchangeReq = TokenExchangeReqServiceDto(
                        token = authToken,
                        redirectUri = redirect,
                        verifier = verifier
                    )
                    val exchangeRes = keycloakTokenProvider.exchangeToken(exchangeReq)

                    val refreshRes = keycloakTokenProvider.refreshToken(exchangeRes.refreshToken)

                    refreshRes.accessToken shouldNotBe exchangeRes.accessToken
                    refreshRes.refreshToken shouldNotBe exchangeRes.refreshToken
                }
                scenario("잘못된 Refresh Token 이 주어지면, 예외를 던진다.") {
                    val invalidRefreshToken = "invalid refresh token"

                    val ex = assertThrows<BadToken> {
                        keycloakTokenProvider.refreshToken(invalidRefreshToken)
                    }
                }
            }

            feature("KeycloakTokenProvider.revokeToken"){
                scenario("정상적인 Refresh Token 이 주어지면, 토큰을 폐기한다.") {
                    val testUser = TestUser.USER
                    val tokens = testKeycloakUtil.getTokens(testUser)

                    keycloakTokenProvider.revokeToken(tokens.refreshToken)

                    testKeycloakUtil.validationAccessTokenValid(tokens.accessToken) shouldBe false
                }
                scenario("잘못된 Refresh Token 이 주어지면, 무시된다.") {
                    val invalidRefreshToken = "invalid refresh token"

                    keycloakTokenProvider.revokeToken(invalidRefreshToken)

                }
            }
        }

    }
}

