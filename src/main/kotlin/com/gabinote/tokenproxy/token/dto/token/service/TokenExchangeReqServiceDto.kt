package com.gabinote.tokenproxy.token.dto.token.service

data class TokenExchangeReqServiceDto(
    val token: String,
    val verifier: String,
    val redirectUri: String,
)