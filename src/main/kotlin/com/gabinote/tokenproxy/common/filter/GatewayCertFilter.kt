package com.gabinote.tokenproxy.common.filter

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val logger = KotlinLogging.logger {}

/**
 * 게이트웨이 인증 필터
 * 모든 API 요청에 대해 게이트웨이에서 설정한 시크릿 헤더(X-Gateway-Secret)를 검증
 * 시크릿이 일치하지 않으면 403 Forbidden 응답 반환
 *
 * @property gatewaySecret 게이트웨이 시크릿 값 (gabinote.common.gateway.secret)
 */
@Profile("!test")
@Component
class GatewayCertFilter(
    @Value("\${gabinote.common.gateway.secret}")
    private val gatewaySecret: String,
) : OncePerRequestFilter() {

    private val gatewaySecretHeader = "X-Gateway-Secret"

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {

        val requestSecret = request.getHeader(gatewaySecretHeader)
        if (requestSecret != gatewaySecret) {
            logger.warn { "Gateway certification failed. Invalid secret: $requestSecret" }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")
            return
        }
        filterChain.doFilter(request, response)

    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath

        return !path.startsWith("/api/")
    }
}