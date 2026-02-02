package com.gabinote.tokenproxy.keycloak.auth.provider

import com.gabinote.tokenproxy.common.config.properties.KeycloakProperties
import com.gabinote.tokenproxy.common.util.exception.service.BadToken
import com.gabinote.tokenproxy.common.util.exception.service.ResourceForbidden
import com.gabinote.tokenproxy.common.util.exception.service.ServerError
import com.gabinote.tokenproxy.keycloak.auth.provider.data.TokenExchangeKeycloakRes
import com.gabinote.tokenproxy.token.auth.provider.TokenProvider
import com.gabinote.tokenproxy.token.dto.token.service.RedirectIdpReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeReqServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenExchangeResServiceDto
import com.gabinote.tokenproxy.token.dto.token.service.TokenRefreshResServiceDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder


private val logger = KotlinLogging.logger {}

@Component
class KeycloakTokenProvider(
    private val props: KeycloakProperties,
    private val restClientBuilder: RestClient.Builder
) : TokenProvider {

    private val restClient = restClientBuilder
        .baseUrl(props.serverUrl)
        .build()

    override fun generateIdpUri(dto: RedirectIdpReqServiceDto): String {
        val uriBuilder = buildIdpUri(dto)

        return uriBuilder.toUriString()
    }


    override fun exchangeToken(dto: TokenExchangeReqServiceDto): TokenExchangeResServiceDto {
        val uriBuilder = buildTokenExchangeUri()

        val formData = buildTokenExchangeForm(dto)

        val res = requestExchangeToken(uriBuilder, formData)

        return TokenExchangeResServiceDto(
            accessToken = res.accessToken,
            refreshToken = res.refreshToken,
            accessTokenExpiresIn = res.expiresIn,
            refreshTokenExpiresIn = res.refreshExpiresIn.toLong()
        )
    }

    override fun refreshToken(refreshToken: String): TokenRefreshResServiceDto {
        val uriBuilder = buildTokenExchangeUri()

        val formData = buildRefreshTokenForm(refreshToken)

        val res = requestExchangeToken(uriBuilder, formData)

        return TokenRefreshResServiceDto(
            accessToken =  res.accessToken,
            refreshToken = res.refreshToken,
            accessTokenExpiresIn = res.expiresIn,
            refreshTokenExpiresIn = res.refreshExpiresIn.toLong()
        )
    }

    override fun revokeToken(refreshToken: String) {
        val uriBuilder = buildRevokeUri()

        val formData = buildRevokeForm(refreshToken)

        requestRevokeToken(uriBuilder, formData)
    }


    private fun buildRevokeUri(): UriComponentsBuilder = UriComponentsBuilder.fromUriString(props.serverUrl).apply {
        pathSegment("realms", props.realm, "protocol", "openid-connect", "logout")
    }


    private fun buildTokenExchangeUri(): UriComponentsBuilder {
        val uriBuilder = UriComponentsBuilder.fromUriString(props.serverUrl).apply {
            pathSegment("realms", props.realm, "protocol", "openid-connect", "token")
        }
        return uriBuilder
    }


    private fun buildIdpUri(dto: RedirectIdpReqServiceDto): UriComponentsBuilder {
        val uriBuilder = UriComponentsBuilder.fromUriString(props.serverUrl).apply {
            pathSegment("realms", props.realm, "protocol", "openid-connect", "auth")
            queryParam("client_id", props.clientId)
            queryParam("redirect_uri", dto.redirectUri)
            queryParam("response_type", "code")
            queryParam("scope", "openid")
            queryParam("code_challenge", dto.codeChallenge)
            queryParam("code_challenge_method", "S256")
            queryParam("kc_idp_hint", dto.idpHint)
        }
        return uriBuilder
    }


    private fun buildRefreshTokenForm(refreshToken: String): LinkedMultiValueMap<String, String> {
        return LinkedMultiValueMap<String, String>().apply {
            set("grant_type", "refresh_token")
            set("client_id", props.clientId)
            set("client_secret", props.clientSecret)
            set("refresh_token", refreshToken)
        }
    }

    private fun buildRevokeForm(refreshToken: String): LinkedMultiValueMap<String, String>{
        return LinkedMultiValueMap<String, String>().apply {
            set("client_id", props.clientId)
            set("client_secret", props.clientSecret)
            set("refresh_token", refreshToken)
        }
    }

    private fun buildTokenExchangeForm(dto: TokenExchangeReqServiceDto):LinkedMultiValueMap<String, String> {
        return LinkedMultiValueMap<String, String>().apply {
            set("grant_type", "authorization_code")
            set("client_id", props.clientId)
            set("client_secret", props.clientSecret)
            set("code", dto.token)
            set("redirect_uri", dto.redirectUri)
            set("code_verifier", dto.verifier)
        }
    }


    private fun requestExchangeToken(
        uriBuilder: UriComponentsBuilder,
        formData:LinkedMultiValueMap<String, String>,
    ) : TokenExchangeKeycloakRes {
        var response: TokenExchangeKeycloakRes?
        try {
             response = restClient.post().apply {
                uri(uriBuilder.build().toUri())
                contentType(MediaType.APPLICATION_FORM_URLENCODED)
                body(formData)
            }.retrieve().body<TokenExchangeKeycloakRes>()
        } catch (e: HttpClientErrorException) {
            if(e.statusCode in  listOf(HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN)){
                throw BadToken(logMessage = e.message ?: "Unknown error")
            }else{
                throw ServerError(reason = "Unknown error from Keycloak")
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to exchange token from Keycloak" }
            throw ServerError(reason = "Unknown error from Keycloak")
        }

        response?: throw ServerError("Failed to exchange token from Keycloak")

        return response
    }



    private fun requestRevokeToken(
        uriBuilder: UriComponentsBuilder,
        formData: LinkedMultiValueMap<String, String>,
    ) {
        try {
            restClient.post().apply {
                uri(uriBuilder.build().toUri())
                contentType(MediaType.APPLICATION_FORM_URLENCODED)
                body(formData)
            }.retrieve().body<Void>()
        } catch (e: HttpClientErrorException) {

            // 5xx 에러라면 서버 에러로 간주하고 예외 던짐
            if(e.statusCode == HttpStatus.INTERNAL_SERVER_ERROR) {
                throw ServerError(reason = "Unknown error from Keycloak")
            }
            // 실패하더라도 4xx 에러라면 무시함.
            // 토큰이 이미 만료되었거나 잘못된 경우일 수 있음.
            // 그렇기에 그냥 넘기고 로그 남기고, 쿠키 제거 처리만 함.
            logger.warn(e) { "Failed to revoke token from Keycloak" }
        } catch (e: Exception) {
            //
            logger.error(e) { "Failed to revoke token from Keycloak" }
            throw ServerError(reason = "Unknown error from Keycloak")
        }

    }


}