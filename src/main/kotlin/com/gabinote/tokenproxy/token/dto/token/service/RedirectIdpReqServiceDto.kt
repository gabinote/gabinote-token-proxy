package com.gabinote.tokenproxy.token.dto.token.service

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

data class RedirectIdpReqServiceDto(
    val redirectUri: String,
    val codeChallenge: String,
    val idpHint: String,
)