package com.gabinote.tokenproxy.token.dto.token.service

data class TokenExchangeResServiceDto(
    val accessToken: String,
    val accessTokenExpiresIn: Long,

    val refreshToken: String,
    val refreshTokenExpiresIn: Long
)