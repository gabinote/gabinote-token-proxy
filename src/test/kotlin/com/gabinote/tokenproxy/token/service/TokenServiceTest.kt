package com.gabinote.tokenproxy.token.service

import com.gabinote.tokenproxy.common.config.properties.TokenProperties
import com.gabinote.tokenproxy.common.util.exception.service.ResourceNotValid
import com.gabinote.tokenproxy.testSupport.testTemplate.ServiceTestTemplate
import com.gabinote.tokenproxy.token.auth.provider.TokenProvider
import com.gabinote.tokenproxy.token.dto.token.service.RedirectIdpReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeResServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenRefreshResServiceDto
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.assertThrows

class TokenServiceTest : ServiceTestTemplate() {

    private lateinit var tokenService: TokenService

    @MockK
    private lateinit var tokenProvider: TokenProvider

    @MockK
    private lateinit var tokenProperties: TokenProperties

    init {
        beforeTest {
            clearAllMocks()
            tokenService = TokenService(
                tokenProvider,
                tokenProperties
            )
        }

        describe("[Token] TokenService Test") {
            describe("TokenService.generateIdpUri") {
                describe("성공 케이스") {
                    context("올바른 redirectUri와 idpHint가 주어지면,") {
                        val allowedRedirectUri = "https://example.com"
                        val allowedIdpHints = listOf("google", "kakao")
                        val dto = RedirectIdpReqServiceDto(
                            redirectUri = "https://example.com/callback",
                            codeChallenge = "test-code-challenge",
                            idpHint = "google"
                        )
                        val expectedUri = "https://keycloak.example.com/auth?redirect_uri=${dto.redirectUri}"

                        beforeTest {
                            every { tokenProperties.allowedRedirectUri } returns allowedRedirectUri
                            every { tokenProperties.allowedIdpHints } returns allowedIdpHints
                            every { tokenProvider.generateIdpUri(dto) } returns expectedUri
                        }

                        it("IDP 리다이렉트 URI를 반환한다.") {
                            val result = tokenService.generateIdpUri(dto)

                            result shouldBe expectedUri

                            verify(exactly = 1) { tokenProperties.allowedRedirectUri }
                            verify(exactly = 1) { tokenProperties.allowedIdpHints }
                            verify(exactly = 1) { tokenProvider.generateIdpUri(dto) }
                        }
                    }
                }

                describe("실패 케이스") {
                    context("허용되지 않은 redirectUri가 주어지면,") {
                        val allowedRedirectUri = "https://example.com"
                        val dto = RedirectIdpReqServiceDto(
                            redirectUri = "https://malicious.com/callback",
                            codeChallenge = "test-code-challenge",
                            idpHint = "google"
                        )

                        beforeTest {
                            every { tokenProperties.allowedRedirectUri } returns allowedRedirectUri
                        }

                        it("ResourceNotValid 예외를 던진다.") {
                            val ex = assertThrows<ResourceNotValid> {
                                tokenService.generateIdpUri(dto)
                            }

                            ex.name shouldBe "redirect_uri"
                            ex.reasons[0] shouldBe "The redirect_uri is not allowed. Allowed redirect_uri must start with $allowedRedirectUri"

                            verify(exactly = 1) { tokenProperties.allowedRedirectUri }
                        }
                    }

                    context("허용되지 않은 idpHint가 주어지면,") {
                        val allowedRedirectUri = "https://example.com"
                        val allowedIdpHints = listOf("google", "kakao")
                        val dto = RedirectIdpReqServiceDto(
                            redirectUri = "https://example.com/callback",
                            codeChallenge = "test-code-challenge",
                            idpHint = "facebook"
                        )

                        beforeTest {
                            every { tokenProperties.allowedRedirectUri } returns allowedRedirectUri
                            every { tokenProperties.allowedIdpHints } returns allowedIdpHints
                        }

                        it("ResourceNotValid 예외를 던진다.") {
                            val ex = assertThrows<ResourceNotValid> {
                                tokenService.generateIdpUri(dto)
                            }

                            ex.name shouldBe "idp_hint"
                            ex.reasons[0] shouldBe "The idp_hint is not allowed. Allowed idp_hints are $allowedIdpHints"

                            verify(exactly = 1) { tokenProperties.allowedRedirectUri }
                            verify(exactly = 1) { tokenProperties.allowedIdpHints }
                        }
                    }
                }
            }

            describe("TokenService.exchangeToken") {
                describe("성공 케이스") {
                    context("올바른 정보가 주어지면,") {
                        val allowedRedirectUri = "https://example.com"
                        val dto = TokenExchangeReqServiceDto(
                            token = "auth-code",
                            verifier = "code-verifier",
                            redirectUri = "https://example.com/callback"
                        )
                        val expectedRes = TokenExchangeResServiceDto(
                            accessToken = "access-token",
                            refreshToken = "refresh-token",
                            accessTokenExpiresIn = 300L,
                            refreshTokenExpiresIn = 1800L
                        )

                        beforeTest {
                            every { tokenProperties.allowedRedirectUri } returns allowedRedirectUri
                            every { tokenProvider.exchangeToken(dto) } returns expectedRes
                        }

                        it("토큰 교환 결과를 반환한다.") {
                            val result = tokenService.exchangeToken(dto)

                            result shouldBe expectedRes

                            verify(exactly = 1) { tokenProperties.allowedRedirectUri }
                            verify(exactly = 1) { tokenProvider.exchangeToken(dto) }
                        }
                    }
                }

                describe("실패 케이스") {
                    context("허용되지 않은 redirectUri가 주어지면,") {
                        val allowedRedirectUri = "https://example.com"
                        val dto = TokenExchangeReqServiceDto(
                            token = "auth-code",
                            verifier = "code-verifier",
                            redirectUri = "https://malicious.com/callback"
                        )

                        beforeTest {
                            every { tokenProperties.allowedRedirectUri } returns allowedRedirectUri
                        }

                        it("ResourceNotValid 예외를 던진다.") {
                            val ex = assertThrows<ResourceNotValid> {
                                tokenService.exchangeToken(dto)
                            }

                            ex.name shouldBe "redirect_uri"
                            ex.reasons[0] shouldBe "The redirect_uri is not allowed. Allowed redirect_uri must start with $allowedRedirectUri"

                            verify(exactly = 1) { tokenProperties.allowedRedirectUri }
                        }
                    }
                }
            }

            describe("TokenService.refreshToken") {
                context("올바른 refreshToken이 주어지면,") {
                    val refreshToken = "valid-refresh-token"
                    val expectedRes = TokenRefreshResServiceDto(
                        accessToken = "new-access-token",
                        refreshToken = "new-refresh-token",
                        accessTokenExpiresIn = 300L,
                        refreshTokenExpiresIn = 1800L
                    )

                    beforeTest {
                        every { tokenProvider.refreshToken(refreshToken) } returns expectedRes
                    }

                    it("새로운 토큰 정보를 반환한다.") {
                        val result = tokenService.refreshToken(refreshToken)

                        result shouldBe expectedRes

                        verify(exactly = 1) { tokenProvider.refreshToken(refreshToken) }
                    }
                }
            }

            describe("TokenService.revokeToken") {
                context("올바른 refreshToken이 주어지면,") {
                    val refreshToken = "valid-refresh-token"

                    beforeTest {
                        every { tokenProvider.revokeToken(refreshToken) } returns Unit
                    }

                    it("토큰을 무효화한다.") {
                        tokenService.revokeToken(refreshToken)

                        verify(exactly = 1) { tokenProvider.revokeToken(refreshToken) }
                    }
                }
            }
        }
    }
}

