package com.devpilot.backend.common.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class TokenDtoRequest(
    @field:NotBlank
    @JsonProperty("refreshToken")
    val refreshToken: String,
)
