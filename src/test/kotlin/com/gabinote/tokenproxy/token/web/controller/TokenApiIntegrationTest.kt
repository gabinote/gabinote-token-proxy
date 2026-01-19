package com.gabinote.tokenproxy.token.web.controller

import com.gabinote.tokenproxy.testSupport.testTemplate.IntegrationTestTemplate
import com.gabinote.tokenproxy.testSupport.testUtil.keycloak.PkceHelper
import com.gabinote.tokenproxy.testSupport.testUtil.keycloak.TestUser
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.*
import org.springframework.beans.factory.annotation.Value

class TokenApiIntegrationTest : IntegrationTestTemplate() {

    @Value("\${keycloak.proxy.refresh-cookie.name}")
    lateinit var refreshCookieName: String

    @Value("\${keycloak.proxy.refresh-cookie.max-age}")
    var refreshCookieMaxAge: Long = 0

    @Value("\${keycloak.proxy.refresh-cookie.allow-path}")
    lateinit var refreshCookiePath: String

    init {
        feature("[Token] Token API Integration Test") {

            feature("[POST] /api/v1/token/idp-login") {
                scenario("정상적인 IDP 로그인 요청이 주어지면, Keycloak IDP 인증 URI로 리다이렉트한다.") {
                    val verifier = PkceHelper.generateCodeVerifier()
                    val challenge = PkceHelper.generateCodeChallenge(verifier)
                    val redirectUri = "http://localhost:8080/tmp"
                    val idpHint = "google"

                    Given {
                        basePath(apiPrefix)
                        contentType("application/json")
                        body(
                            mapOf(
                                "redirect_uri" to redirectUri,
                                "code_challenge" to challenge,
                                "idp_hint" to idpHint
                            )
                        )
                    }.When {
                        post("/token/idp-login")
                    }.Then {
                        statusCode(302)
                        header("Location", containsString("protocol/openid-connect/auth"))
                        header("Location", containsString("redirect_uri=$redirectUri"))
                        header("Location", containsString("code_challenge=$challenge"))
                        header("Location", containsString("kc_idp_hint=$idpHint"))
                    }
                }

                scenario("redirect_uri가 빈 문자열이면, 400 Bad Request를 응답한다.") {
                    val verifier = PkceHelper.generateCodeVerifier()
                    val challenge = PkceHelper.generateCodeChallenge(verifier)

                    Given {
                        basePath(apiPrefix)
                        contentType("application/json")
                        body(
                            mapOf(
                                "redirect_uri" to "",
                                "code_challenge" to challenge,
                                "idp_hint" to "google"
                            )
                        )
                    }.When {
                        post("/token/idp-login")
                    }.Then {
                        statusCode(400)
                    }
                }

                scenario("code_challenge가 누락되면, 400 Bad Request를 응답한다.") {
                    Given {
                        basePath(apiPrefix)
                        contentType("application/json")
                        body(
                            mapOf(
                                "redirect_uri" to "https://example.com/callback",
                                "idp_hint" to "google"
                            )
                        )
                    }.When {
                        post("/token/idp-login")
                    }.Then {
                        statusCode(400)
                    }
                }

                scenario("idp_hint가 누락되면, 400 Bad Request를 응답한다.") {
                    val verifier = PkceHelper.generateCodeVerifier()
                    val challenge = PkceHelper.generateCodeChallenge(verifier)

                    Given {
                        basePath(apiPrefix)
                        contentType("application/json")
                        body(
                            mapOf(
                                "redirect_uri" to "https://example.com/callback",
                                "code_challenge" to challenge
                            )
                        )
                    }.When {
                        post("/token/idp-login")
                    }.Then {
                        statusCode(400)
                    }
                }
            }

            feature("[POST] /api/v1/token/exchange") {
                scenario("정상적인 Authorization Code가 주어지면, Access Token과 Refresh Token을 발급한다.") {
                    val testUser = TestUser.USER
                    val verifier = PkceHelper.generateCodeVerifier()
                    val challenge = PkceHelper.generateCodeChallenge(verifier)
                    val redirectUri = "http://localhost:8080/tmp"
                    val authCode = testKeycloakUtil.getAuthCode(redirectUri, testUser.id, testUser.password, challenge)

                    val response = Given {
                        basePath(apiPrefix)
                        contentType("application/json")
                        body(
                            mapOf(
                                "token" to authCode,
                                "verifier" to verifier,
                                "redirect_uri" to redirectUri
                            )
                        )
                    }.When {
                        post("/token/exchange")
                    }.Then {
                        statusCode(200)
                        body("access_token", notNullValue())
                        body("expires_in", greaterThan(0))
                    }.Extract {
                        response()
                    }

                    val accessToken = response.jsonPath().getString("access_token")
                    testKeycloakUtil.validationAccessTokenValid(accessToken) shouldBe true

                    // Set-Cookie 헤더에서 쿠키 속성 검증
                    val setCookieHeader = response.header("Set-Cookie")
                    setCookieHeader shouldNotBe null
                    setCookieHeader shouldContainCookieAttribute refreshCookieName
                    setCookieHeader shouldContainCookieAttribute "HttpOnly"
                    setCookieHeader shouldContainCookieAttribute "Secure"
                    setCookieHeader shouldContainCookieAttribute "SameSite=Strict"
                    setCookieHeader shouldContainCookieAttribute "Path=$refreshCookiePath"
                    setCookieHeader shouldContainCookieAttribute "Max-Age=$refreshCookieMaxAge"
                }

                scenario("잘못된 Authorization Code가 주어지면, 401 Unauthorized를 응답한다.") {
                    val verifier = PkceHelper.generateCodeVerifier()
                    Given {
                        basePath(apiPrefix)
                        contentType("application/json")
                        body(
                            mapOf(
                                "token" to "invalid-auth-code",
                                "verifier" to verifier,
                                "redirect_uri" to "http://localhost:8080/tmp"
                            )
                        )
                    }.When {
                        post("/token/exchange")
                    }.Then {
                        statusCode(401)
                    }
                }

                scenario("token이 빈 문자열이면, 400 Bad Request를 응답한다.") {
                    val verifier = PkceHelper.generateCodeVerifier()

                    Given {
                        basePath(apiPrefix)
                        contentType("application/json")
                        body(
                            mapOf(
                                "token" to "",
                                "verifier" to verifier,
                                "redirect_uri" to "https://example.com/callback"
                            )
                        )
                    }.When {
                        post("/token/exchange")
                    }.Then {
                        statusCode(400)
                    }
                }

                scenario("verifier가 누락되면, 400 Bad Request를 응답한다.") {
                    Given {
                        basePath(apiPrefix)
                        contentType("application/json")
                        body(
                            mapOf(
                                "token" to "auth-code",
                                "redirect_uri" to "https://example.com/callback"
                            )
                        )
                    }.When {
                        post("/token/exchange")
                    }.Then {
                        statusCode(400)
                    }
                }

                scenario("redirect_uri가 누락되면, 400 Bad Request를 응답한다.") {
                    val verifier = PkceHelper.generateCodeVerifier()

                    Given {
                        basePath(apiPrefix)
                        contentType("application/json")
                        body(
                            mapOf(
                                "token" to "auth-code",
                                "verifier" to verifier
                            )
                        )
                    }.When {
                        post("/token/exchange")
                    }.Then {
                        statusCode(400)
                    }
                }
            }

            feature("[POST] /api/v1/token/refresh") {
                scenario("정상적인 Refresh Token이 쿠키에 주어지면, 새로운 Access Token을 발급한다.") {
                    val testUser = TestUser.USER
                    val tokens = testKeycloakUtil.getTokens(testUser)

                    val response = Given {
                        basePath(apiPrefix)
                        cookie(refreshCookieName, tokens.refreshToken)
                    }.When {
                        post("/token/refresh")
                    }.Then {
                        statusCode(200)
                        body("access_token", notNullValue())
                        body("expires_in", greaterThan(0))
                    }.Extract {
                        response()
                    }

                    val newAccessToken = response.jsonPath().getString("access_token")
                    testKeycloakUtil.validationAccessTokenValid(newAccessToken) shouldBe true
                    newAccessToken shouldNotBe tokens.accessToken

                    // Set-Cookie 헤더에서 쿠키 속성 검증
                    val setCookieHeader = response.header("Set-Cookie")
                    setCookieHeader shouldNotBe null
                    setCookieHeader shouldContainCookieAttribute refreshCookieName
                    setCookieHeader shouldContainCookieAttribute "HttpOnly"
                    setCookieHeader shouldContainCookieAttribute "Secure"
                    setCookieHeader shouldContainCookieAttribute "SameSite=Strict"
                    setCookieHeader shouldContainCookieAttribute "Path=$refreshCookiePath"
                    setCookieHeader shouldContainCookieAttribute "Max-Age=$refreshCookieMaxAge"
                }

                scenario("잘못된 Refresh Token이 주어지면, 401 Unauthorized를 응답한다.") {
                    Given {
                        basePath(apiPrefix)
                        cookie(refreshCookieName, "invalid-refresh-token")
                    }.When {
                        post("/token/refresh")
                    }.Then {
                        statusCode(401)
                    }
                }

                scenario("Refresh Token 쿠키가 없으면, 401 Unauthorized를 응답한다.") {
                    Given {
                        basePath(apiPrefix)
                    }.When {
                        post("/token/refresh")
                    }.Then {
                        statusCode(401)
                    }
                }
            }

            feature("[POST] /api/v1/token/logout") {
                scenario("정상적인 Refresh Token이 쿠키에 주어지면, 토큰을 폐기하고 쿠키를 삭제한다.") {
                    val testUser = TestUser.USER
                    val tokens = testKeycloakUtil.getTokens(testUser)

                    val response = Given {
                        basePath(apiPrefix)
                        cookie(refreshCookieName, tokens.refreshToken)
                    }.When {
                        post("/token/logout")
                    }.Then {
                        statusCode(204)
                    }.Extract {
                        response()
                    }

                    // 토큰이 폐기되었는지 확인
                    testKeycloakUtil.validationAccessTokenValid(tokens.accessToken) shouldBe false

                    // 쿠키 삭제 확인 (Max-Age=0)
                    val setCookieHeader = response.header("Set-Cookie")
                    setCookieHeader shouldNotBe null
                    setCookieHeader shouldContainCookieAttribute refreshCookieName
                    setCookieHeader shouldContainCookieAttribute "Max-Age=0"
                    setCookieHeader shouldContainCookieAttribute "HttpOnly"
                    setCookieHeader shouldContainCookieAttribute "Secure"
                    setCookieHeader shouldContainCookieAttribute "SameSite=Strict"
                    setCookieHeader shouldContainCookieAttribute "Path=$refreshCookiePath"
                }

                scenario("Refresh Token 쿠키가 없으면, 401 Unauthorized를 응답한다.") {
                    Given {
                        basePath(apiPrefix)
                    }.When {
                        post("/token/logout")
                    }.Then {
                        statusCode(401)
                    }
                }

                scenario("잘못된 Refresh Token이 주어져도, 204 No Content를 응답한다.") {
                    // Keycloak은 잘못된 토큰 폐기 요청에도 에러를 반환하지 않음
                    Given {
                        basePath(apiPrefix)
                        cookie(refreshCookieName, "invalid-refresh-token")
                    }.When {
                        post("/token/logout")
                    }.Then {
                        statusCode(204)
                    }
                }
            }
        }
    }
}

/**
 * Set-Cookie 헤더에 특정 속성이 포함되어 있는지 확인하는 infix 함수
 */
private infix fun String.shouldContainCookieAttribute(attribute: String) {
    if (!this.contains(attribute)) {
        throw AssertionError("Expected Set-Cookie header to contain '$attribute', but was: $this")
    }
}
