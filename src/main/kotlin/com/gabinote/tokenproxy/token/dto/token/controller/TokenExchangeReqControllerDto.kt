package com.gabinote.tokenproxy.token.dto.token.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TokenExchangeReqControllerDto(

    /**
     * Identity Broker Flow에서 발급된 토큰
     */
    @field:NotBlank(message = "token must not be blank")
    val token: String,

    /**
     * 클라이언트쪽 PKCE 검증용 코드
     */
    @field:NotBlank(message = "verifier must not be blank")
    val verifier: String,

    @field:NotBlank(message = "redirect_uri must not be blank")
    @field:URL(message = "redirect_uri must be a valid URL")
    val redirectUri: String,
)