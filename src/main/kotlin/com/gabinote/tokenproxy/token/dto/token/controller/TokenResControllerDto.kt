package com.gabinote.tokenproxy.token.dto.token.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TokenResControllerDto(
    /**
     * 액세스 토큰
     */
    val accessToken: String,

    /**
     * 액세스 토큰 만료 시간 (단위: 초)
     */
    val expiresIn: Long,
)