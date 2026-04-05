package com.ensnif.lanime.domain.admin.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class AdminSigninRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)
