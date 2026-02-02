package com.gabinote.tokenproxy.token.service

import com.gabinote.tokenproxy.common.config.properties.TokenProperties
import com.gabinote.tokenproxy.common.config.properties.TokenRefreshProperties
import com.gabinote.tokenproxy.common.util.exception.service.ResourceNotValid
import com.gabinote.tokenproxy.token.auth.provider.TokenProvider
import com.gabinote.tokenproxy.token.dto.token.service.RedirectIdpReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeResServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenRefreshResServiceDto
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val tokenProvider: TokenProvider,
    private val tokenProperties: TokenProperties,
) {
    fun generateIdpUri(dto: RedirectIdpReqServiceDto): String {
        validateRedirectUri(dto.redirectUri)
        validateIdpHint(dto.idpHint)
        return tokenProvider.generateIdpUri(dto)
    }

    fun exchangeToken(dto: TokenExchangeReqServiceDto): TokenExchangeResServiceDto {
        validateRedirectUri(dto.redirectUri)
        return tokenProvider.exchangeToken(dto)
    }

    fun refreshToken(refreshToken: String): TokenRefreshResServiceDto {
        return tokenProvider.refreshToken(refreshToken)
    }

    fun revokeToken(refreshToken: String) {
        return tokenProvider.revokeToken(refreshToken)
    }

    private fun validateRedirectUri(redirectUri: String) {
        val allowedRedirectUri = tokenProperties.allowedRedirectUri
        if (!redirectUri.startsWith(allowedRedirectUri)) {
            throw ResourceNotValid(
                name = "redirect_uri",
                reasons = listOf("The redirect_uri is not allowed. Allowed redirect_uri must start with $allowedRedirectUri")
            )
        }
    }

    private fun validateIdpHint(idpHint: String) {
        val allowedIdpHints = tokenProperties.allowedIdpHints
        if (!allowedIdpHints.contains(idpHint)) {
            throw ResourceNotValid(
                name = "idp_hint",
                reasons = listOf("The idp_hint is not allowed. Allowed idp_hints are $allowedIdpHints")
            )
        }
    }
}