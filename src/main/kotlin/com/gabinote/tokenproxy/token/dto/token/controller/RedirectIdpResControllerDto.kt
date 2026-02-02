package com.gabinote.tokenproxy.token.dto.token.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RedirectIdpResControllerDto(

    val url: String,
)