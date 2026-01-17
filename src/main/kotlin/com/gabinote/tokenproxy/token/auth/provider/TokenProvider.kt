package com.gabinote.tokenproxy.token.auth.provider

import com.gabinote.tokenproxy.token.dto.token.service.RedirectIdpReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeResServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenRefreshResServiceDto

/**
 * 토큰 제공자 인터페이스
 */
interface TokenProvider {
    /**
     * IDP(Identity Provider)로 리다이렉트할 URL 생성
     * @param dto 리다이렉트 요청 데이터
     * @return 리다이렉트할 URL
     */
    fun generateIdpUrl(dto: RedirectIdpReqServiceDto): String

    /**
     * Identity Broker Login Flow 에서 발급된 토큰을 Access Token, Refresh Token 으로 교환
     * @param dto 토큰 교환 요청 데이터
     * @return Access Token, Refresh Token
     */
    fun exchangeToken(dto: TokenExchangeReqServiceDto): TokenExchangeResServiceDto

    /**
     * Refresh Token 으로 새로운 Access Token, Refresh Token 발급
     * @param refreshToken 갱신할 Refresh Token
     * @return 새로운 Access Token, Refresh Token
     */
    fun refreshToken(refreshToken: String): TokenRefreshResServiceDto

    /**
     * Refresh Token 무효화
     * @param refreshToken 무효화할 Refresh Token
     */
    fun revokeToken(refreshToken: String)
}