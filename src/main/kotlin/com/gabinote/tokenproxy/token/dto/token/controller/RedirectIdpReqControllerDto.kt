package com.gabinote.tokenproxy.token.dto.token.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RedirectIdpReqControllerDto(

    @field:NotBlank(message = "redirect_uri must not be blank")
    @field:URL(message = "redirect_uri must be a valid URL")
    val redirectUri: String,

    @field:NotBlank(message = "redirect_uri must not be blank")
    val codeChallenge: String,

    @field:NotBlank(message = "idp_hint must not be blank")
    val idpHint: String,
)