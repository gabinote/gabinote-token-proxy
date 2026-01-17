package com.gabinote.tokenproxy.token.mapping.token

import com.gabinote.tokenproxy.testSupport.testTemplate.MockkTestTemplate
import com.gabinote.tokenproxy.token.dto.token.controller.TokenExchangeReqControllerDto
import com.gabinote.tokenproxy.token.dto.token.controller.TokenResControllerDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeResServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenRefreshResServiceDto
import org.junit.jupiter.api.Assertions.*

class TokenMapperTest  : MockkTestTemplate() {
    private val tokenMapperImpl: TokenMapper = TokenMapperImpl()

    init {
        describe("[Token] TokenMapper Test") {
            describe("TokenMapper.toExchangeResControllerDto"){
                context("TokenExchangeResServiceDto dto가 주어지면"){
                    val dto = TokenExchangeResServiceDto(
                        accessToken = "accessToken",
                        accessTokenExpiresIn = 3600L,
                        refreshToken = "refreshToken",
                        refreshTokenExpiresIn = 7200L
                    )

                    val expected = TokenResControllerDto(
                        accessToken = "accessToken",
                        expiresIn = 3600L,
                    )

                    it("Access 토큰과, 해당 토큰의 만료 시기가 들어있는 TokenResControllerDto 로 매핑한다") {
                        val result = tokenMapperImpl.toExchangeResControllerDto(dto)

                        assertEquals(expected, result)
                    }
                }
            }
            describe("TokenMapper.toRefreshResControllerDto") {
                context("TokenRefreshResServiceDto dto가 주어지면"){
                    val dto = TokenRefreshResServiceDto(
                        accessToken = "accessToken",
                        accessTokenExpiresIn = 3600L,
                        refreshToken = "refreshToken",
                        refreshTokenExpiresIn = 7200L
                    )

                    val expected = TokenResControllerDto(
                        accessToken = "accessToken",
                        expiresIn = 3600L,
                    )

                    it("Access 토큰과, 해당 토큰의 만료 시기가 들어있는 TokenResControllerDto 로 매핑한다") {
                        val result = tokenMapperImpl.toRefreshResControllerDto(dto)

                        assertEquals(expected, result)
                    }
                }
            }
            describe("TokenMapper.toExchangeReqServiceDto") {
                context("TokenExchangeReqControllerDto dto가 주어지면") {
                    val dto = TokenExchangeReqControllerDto(
                        token = "authorizationCode",
                        redirectUri = "https://example.com/callback",
                        verifier = "verifierString"
                    )
                    val expected = com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeReqServiceDto(
                        token = "authorizationCode",
                        redirectUri = "https://example.com/callback",
                        verifier = "verifierString"
                    )
                    it("TokenExchangeReqServiceDto 로 매핑한다") {
                        val result = tokenMapperImpl.toExchangeReqServiceDto(dto)

                        assertEquals(expected, result)
                    }
                }
            }
        }
    }
}