package com.gabinote.tokenproxy.token.web.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.epages.restdocs.apispec.SimpleType
import com.fasterxml.jackson.databind.ObjectMapper
import com.gabinote.tokenproxy.common.config.properties.TokenRefreshProperties
import com.gabinote.tokenproxy.common.web.advice.ControllerExceptionAdvice
import com.gabinote.tokenproxy.common.web.advice.GlobalExceptionAdvice
import com.gabinote.tokenproxy.token.dto.token.controller.RedirectIdpReqControllerDto
import com.gabinote.tokenproxy.token.dto.token.controller.RedirectIdpResControllerDto
import com.gabinote.tokenproxy.token.dto.token.controller.TokenExchangeReqControllerDto
import com.gabinote.tokenproxy.token.dto.token.controller.TokenResControllerDto
import com.gabinote.tokenproxy.token.dto.token.service.RedirectIdpReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeResServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenRefreshResServiceDto
import com.gabinote.tokenproxy.token.mapping.token.TokenMapper
import com.gabinote.tokenproxy.token.service.TokenService
import com.gabinote.tokenproxy.testSupport.testTemplate.WebMvcTestTemplate
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import jakarta.servlet.http.Cookie
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [TokenApiController::class])
@Import(ControllerExceptionAdvice::class, GlobalExceptionAdvice::class)
class TokenApiControllerTest : WebMvcTestTemplate() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var tokenService: TokenService

    @MockkBean
    private lateinit var tokenMapper: TokenMapper

    @MockkBean
    private lateinit var refreshCookieProp: TokenRefreshProperties

    private val apiPrefix = "/api/v1/token"

    init {
        beforeTest {
            every { refreshCookieProp.name } returns "GABI_TEST_PROXY_REFRESH_TOKEN"
            every { refreshCookieProp.maxAge } returns 1209600L
            every { refreshCookieProp.allowPath } returns "/"
            every { refreshCookieProp.allowDomain } returns "localhost"
        }

        describe("[Token] TokenApiController Test") {

            describe("TokenApiController.refreshToken") {
                context("올바른 리프레시 토큰 쿠키가 주어지면") {
                    val refreshToken = "valid-refresh-token"
                    val newAccessToken = "new-access-token"
                    val newRefreshToken = "new-refresh-token"
                    val serviceRes = TokenRefreshResServiceDto(
                        accessToken = newAccessToken,
                        accessTokenExpiresIn = 300L,
                        refreshToken = newRefreshToken,
                        refreshTokenExpiresIn = 1800L
                    )
                    val controllerRes = TokenResControllerDto(
                        accessToken = newAccessToken,
                        expiresIn = 300L
                    )

                    beforeTest {
                        every { tokenService.refreshToken(refreshToken) } returns serviceRes
                        every { tokenMapper.toRefreshResControllerDto(serviceRes) } returns controllerRes
                    }

                    it("토큰을 갱신하고, 200 OK를 응답한다") {
                        mockMvc.perform(
                            post("$apiPrefix/refresh")
                                .cookie(Cookie("GABI_TEST_PROXY_REFRESH_TOKEN", refreshToken))
                        )
                            .andDo(print())
                            .andExpect(status().isOk)
                            .andExpect(content().json(objectMapper.writeValueAsString(controllerRes)))
                            .andExpect(header().exists("Set-Cookie"))
                            .andDo(
                                document(
                                    "token/refreshToken",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Token")
                                            .description("토큰 갱신")
                                            .responseFields(
                                                fieldWithPath("access_token").type(SimpleType.STRING)
                                                    .description("새 액세스 토큰"),
                                                fieldWithPath("expires_in").type(SimpleType.NUMBER)
                                                    .description("액세스 토큰 만료 시간 (초)")
                                            )
                                            .responseSchema(Schema("TokenResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) {
                            tokenService.refreshToken(refreshToken)
                            tokenMapper.toRefreshResControllerDto(serviceRes)
                        }
                    }
                }

                context("리프레시 토큰 쿠키가 없으면") {
                    it("403 Forbidden을 응답한다") {
                        mockMvc.perform(
                            post("$apiPrefix/refresh")
                        )
                            .andDo(print())
                            .andExpect(status().isUnauthorized)
                    }
                }
            }

            describe("TokenApiController.exchangeToken") {
                context("올바른 요청이 주어지면") {
                    val reqDto = TokenExchangeReqControllerDto(
                        token = "auth-code",
                        verifier = "code-verifier",
                        redirectUri = "https://example.com/callback"
                    )
                    val serviceReqDto = TokenExchangeReqServiceDto(
                        token = reqDto.token,
                        verifier = reqDto.verifier,
                        redirectUri = reqDto.redirectUri
                    )
                    val serviceRes = TokenExchangeResServiceDto(
                        accessToken = "access-token",
                        accessTokenExpiresIn = 300L,
                        refreshToken = "refresh-token",
                        refreshTokenExpiresIn = 1800L
                    )
                    val controllerRes = TokenResControllerDto(
                        accessToken = "access-token",
                        expiresIn = 300L
                    )

                    beforeTest {
                        every { tokenMapper.toExchangeReqServiceDto(reqDto) } returns serviceReqDto
                        every { tokenService.exchangeToken(serviceReqDto) } returns serviceRes
                        every { tokenMapper.toExchangeResControllerDto(serviceRes) } returns controllerRes
                    }

                    it("토큰을 교환하고, 200 OK를 응답한다") {
                        mockMvc.perform(
                            post("$apiPrefix/exchange")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                        )
                            .andDo(print())
                            .andExpect(status().isOk)
                            .andExpect(content().json(objectMapper.writeValueAsString(controllerRes)))
                            .andExpect(header().exists("Set-Cookie"))
                            .andDo(
                                document(
                                    "token/exchangeToken",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Token")
                                            .description("토큰 교환")
                                            .requestFields(
                                                fieldWithPath("token").type(SimpleType.STRING)
                                                    .description("Identity Broker Flow에서 발급된 토큰"),
                                                fieldWithPath("verifier").type(SimpleType.STRING)
                                                    .description("PKCE 검증용 코드"),
                                                fieldWithPath("redirect_uri").type(SimpleType.STRING)
                                                    .description("리다이렉트 URI")
                                            )
                                            .responseFields(
                                                fieldWithPath("access_token").type(SimpleType.STRING)
                                                    .description("액세스 토큰"),
                                                fieldWithPath("expires_in").type(SimpleType.NUMBER)
                                                    .description("액세스 토큰 만료 시간 (초)")
                                            )
                                            .responseSchema(Schema("TokenResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) {
                            tokenMapper.toExchangeReqServiceDto(reqDto)
                            tokenService.exchangeToken(serviceReqDto)
                            tokenMapper.toExchangeResControllerDto(serviceRes)
                        }
                    }
                }

                describe("token 검증 테스트") {
                    context("token이 빈 문자열이면") {
                        val reqDto = mapOf(
                            "token" to "",
                            "verifier" to "code-verifier",
                            "redirect_uri" to "https://example.com/callback"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/exchange")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }

                    context("token이 누락되면") {
                        val reqDto = mapOf(
                            "verifier" to "code-verifier",
                            "redirect_uri" to "https://example.com/callback"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/exchange")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }
                }

                describe("verifier 검증 테스트") {
                    context("verifier가 빈 문자열이면") {
                        val reqDto = mapOf(
                            "token" to "auth-code",
                            "verifier" to "",
                            "redirect_uri" to "https://example.com/callback"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/exchange")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }

                    context("verifier가 누락되면") {
                        val reqDto = mapOf(
                            "token" to "auth-code",
                            "redirect_uri" to "https://example.com/callback"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/exchange")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }
                }

                describe("redirect_uri 검증 테스트") {
                    context("redirect_uri가 빈 문자열이면") {
                        val reqDto = mapOf(
                            "token" to "auth-code",
                            "verifier" to "code-verifier",
                            "redirect_uri" to ""
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/exchange")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }

                    context("redirect_uri가 유효한 URL이 아니면") {
                        val reqDto = mapOf(
                            "token" to "auth-code",
                            "verifier" to "code-verifier",
                            "redirect_uri" to "invalid-url"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/exchange")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }

                    context("redirect_uri가 누락되면") {
                        val reqDto = mapOf(
                            "token" to "auth-code",
                            "verifier" to "code-verifier"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/exchange")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }
                }
            }

            describe("TokenApiController.logoutToken") {
                context("올바른 리프레시 토큰 쿠키가 주어지면") {
                    val refreshToken = "valid-refresh-token"

                    beforeTest {
                        every { tokenService.revokeToken(refreshToken) } returns Unit
                    }

                    it("토큰을 폐기하고, 204 No Content를 응답한다") {
                        mockMvc.perform(
                            post("$apiPrefix/logout")
                                .cookie(Cookie("GABI_TEST_PROXY_REFRESH_TOKEN", refreshToken))
                        )
                            .andDo(print())
                            .andExpect(status().isNoContent)
                            .andExpect(header().exists("Set-Cookie"))
                            .andDo(
                                document(
                                    "token/logoutToken",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Token")
                                            .description("로그아웃 (토큰 폐기)")
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) {
                            tokenService.revokeToken(refreshToken)
                        }
                    }
                }

                context("리프레시 토큰 쿠키가 없으면") {
                    it("401 Unauthorized를 응답한다") {
                        mockMvc.perform(
                            post("$apiPrefix/logout")
                        )
                            .andDo(print())
                            .andExpect(status().isUnauthorized)
                    }
                }
            }

            describe("TokenApiController.getIdpLoginUri") {
                context("올바른 요청이 주어지면") {
                    val reqDto = RedirectIdpReqControllerDto(
                        redirectUri = "https://example.com/callback",
                        codeChallenge = "test-code-challenge",
                        idpHint = "google"
                    )
                    val serviceReqDto = RedirectIdpReqServiceDto(
                        redirectUri = reqDto.redirectUri,
                        codeChallenge = reqDto.codeChallenge,
                        idpHint = reqDto.idpHint
                    )
                    val expectedUri = "https://keycloak.example.com/auth?redirect_uri=${reqDto.redirectUri}"
                    val expectedRes = RedirectIdpResControllerDto(url = expectedUri)

                    beforeTest {
                        every { tokenMapper.toRedirectReqServiceDto(reqDto) } returns serviceReqDto
                        every { tokenService.generateIdpUri(serviceReqDto) } returns expectedUri
                    }

                    it("IDP 로그인 URI를 제공하고, 200 OK를 응답한다") {
                        mockMvc.perform(
                            post("$apiPrefix/idp-login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqDto))
                        )
                            .andDo(print())
                            .andExpect(status().isOk)
                            .andExpect(content().json(objectMapper.writeValueAsString(expectedRes)))
                            .andDo(
                                document(
                                    "token/getIdpLoginUri",
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    resource(
                                        ResourceSnippetParameters
                                            .builder()
                                            .tags("Token")
                                            .description("IDP 로그인 URI 제공")
                                            .requestFields(
                                                fieldWithPath("redirect_uri").type(SimpleType.STRING)
                                                    .description("리다이렉트 URI"),
                                                fieldWithPath("code_challenge").type(SimpleType.STRING)
                                                    .description("PKCE 코드 챌린지"),
                                                fieldWithPath("idp_hint").type(SimpleType.STRING)
                                                    .description("IDP 힌트 (google, kakao 등)")
                                            )
                                            .responseFields(
                                                fieldWithPath("url").type(SimpleType.STRING)
                                                    .description("IDP 로그인 URI")
                                            )
                                            .responseSchema(Schema("RedirectIdpResponse"))
                                            .build()
                                    )
                                )
                            )

                        verify(exactly = 1) {
                            tokenMapper.toRedirectReqServiceDto(reqDto)
                            tokenService.generateIdpUri(serviceReqDto)
                        }
                    }
                }

                describe("redirect_uri 검증 테스트") {
                    context("redirect_uri가 빈 문자열이면") {
                        val reqDto = mapOf(
                            "redirect_uri" to "",
                            "code_challenge" to "test-code-challenge",
                            "idp_hint" to "google"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/idp-login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }

                    context("redirect_uri가 유효한 URL이 아니면") {
                        val reqDto = mapOf(
                            "redirect_uri" to "invalid-url",
                            "code_challenge" to "test-code-challenge",
                            "idp_hint" to "google"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/idp-login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }

                    context("redirect_uri가 누락되면") {
                        val reqDto = mapOf(
                            "code_challenge" to "test-code-challenge",
                            "idp_hint" to "google"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/idp-login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }
                }

                describe("code_challenge 검증 테스트") {
                    context("code_challenge가 빈 문자열이면") {
                        val reqDto = mapOf(
                            "redirect_uri" to "https://example.com/callback",
                            "code_challenge" to "",
                            "idp_hint" to "google"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/idp-login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }

                    context("code_challenge가 누락되면") {
                        val reqDto = mapOf(
                            "redirect_uri" to "https://example.com/callback",
                            "idp_hint" to "google"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/idp-login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }
                }

                describe("idp_hint 검증 테스트") {
                    context("idp_hint가 빈 문자열이면") {
                        val reqDto = mapOf(
                            "redirect_uri" to "https://example.com/callback",
                            "code_challenge" to "test-code-challenge",
                            "idp_hint" to ""
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/idp-login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }

                    context("idp_hint가 누락되면") {
                        val reqDto = mapOf(
                            "redirect_uri" to "https://example.com/callback",
                            "code_challenge" to "test-code-challenge"
                        )

                        it("400 Bad Request를 응답한다") {
                            mockMvc.perform(
                                post("$apiPrefix/idp-login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(reqDto))
                            )
                                .andDo(print())
                                .andExpect(status().isBadRequest)
                        }
                    }
                }
            }
        }
    }
}

