package com.gabinote.tokenproxy.keycloak.auth.provider

import com.gabinote.tokenproxy.token.auth.provider.TokenProvider
import com.gabinote.tokenproxy.token.dto.token.service.RedirectIdpReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeResServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenRefreshResServiceDto
import org.springframework.stereotype.Component

@Component
class KeycloakTokenProvider : TokenProvider {
    override fun generateIdpUrl(dto: RedirectIdpReqServiceDto): String {
        TODO("Not yet implemented")
    }

    override fun exchangeToken(dto: TokenExchangeReqServiceDto): TokenExchangeResServiceDto {
        TODO("Not yet implemented")
    }

    override fun refreshToken(refreshToken: String): TokenRefreshResServiceDto {
        TODO("Not yet implemented")
    }

    override fun revokeToken(refreshToken: String) {
        TODO("Not yet implemented")
    }

}