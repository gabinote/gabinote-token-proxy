package com.gabinote.tokenproxy.token.web.controller

import com.gabinote.tokenproxy.common.config.properties.TokenProperties
import com.gabinote.tokenproxy.common.config.properties.TokenRefreshProperties
import com.gabinote.tokenproxy.common.util.exception.controller.RefreshCookieNotFound
import com.gabinote.tokenproxy.token.dto.token.controller.RedirectIdpReqControllerDto
import com.gabinote.tokenproxy.token.dto.token.controller.RedirectIdpResControllerDto
import com.gabinote.tokenproxy.token.dto.token.controller.TokenExchangeReqControllerDto
import com.gabinote.tokenproxy.token.dto.token.controller.TokenResControllerDto
import com.gabinote.tokenproxy.token.dto.token.service.RedirectIdpReqServiceDto
import com.gabinote.tokenproxy.token.mapping.token.TokenMapper
import com.gabinote.tokenproxy.token.service.TokenService
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.HttpCookie

@Validated
@RequestMapping("/api/v1/token")
@RestController
class TokenApiController(
    private val tokenService: TokenService,
    private val tokenMapper: TokenMapper,
    private val refreshCookieProp: TokenRefreshProperties
) {

    @PostMapping("/refresh")
    fun refreshToken(
        @CookieValue(name = "\${keycloak.proxy.refresh-cookie.name}", required = false)
        refreshToken: String?
    ): ResponseEntity<TokenResControllerDto>{
        refreshToken?: throw RefreshCookieNotFound()

        val tokens = tokenService.refreshToken(refreshToken)
        val cookie = createCookie(tokens.refreshToken)
        val resDto = tokenMapper.toRefreshResControllerDto(tokens)
        return ResponseEntity.ok()
            .header("Set-Cookie", cookie.toString())
            .body(resDto)
    }

    @PostMapping("/exchange")
    fun exchangeToken(
        @Validated
        @RequestBody
        dto : TokenExchangeReqControllerDto
    ): ResponseEntity<TokenResControllerDto>{
        val reqDto = tokenMapper.toExchangeReqServiceDto(dto)

        val tokens = tokenService.exchangeToken(reqDto)
        val cookie = createCookie(tokens.refreshToken)

        val resDto = tokenMapper.toExchangeResControllerDto(tokens)
        return ResponseEntity.ok()
            .header("Set-Cookie", cookie.toString())
            .body(resDto)
    }

    @PostMapping("/logout")
    fun logoutToken(
        @CookieValue(name = "\${keycloak.proxy.refresh-cookie.name}", required = false)
        refreshToken: String?
    ): ResponseEntity<Void> {
        refreshToken?: throw RefreshCookieNotFound()

        tokenService.revokeToken(refreshToken)

        val clearCookie = createClearCookie()
        return ResponseEntity.noContent()
            .header("Set-Cookie", clearCookie.toString())
            .build()
    }

    @PostMapping("/idp-login")
    fun getIdpLoginUri(
        @Validated
        @RequestBody
        dto: RedirectIdpReqControllerDto
    ): ResponseEntity<RedirectIdpResControllerDto>{
        val reqDto = tokenMapper.toRedirectReqServiceDto(dto)
        val uri = tokenService.generateIdpUri(reqDto)
        val res = RedirectIdpResControllerDto(
            url = uri
        )
        return ResponseEntity.ok(res)
    }


    private fun createCookie(refreshToken: String): ResponseCookie {
        val cookie = ResponseCookie.from(refreshCookieProp.name, refreshToken)
            .httpOnly(true)
            .secure(true)
            .path(refreshCookieProp.allowPath)
            .maxAge(refreshCookieProp.maxAge)
            .sameSite("Strict")
            .build()

        return cookie
    }

    private fun createClearCookie(): ResponseCookie{
        val cookie = ResponseCookie.from(refreshCookieProp.name, "")
            .httpOnly(true)
            .secure(true)
            .path(refreshCookieProp.allowPath)
            .maxAge(0)
            .sameSite("Strict")
            .build()

        return cookie
    }
}