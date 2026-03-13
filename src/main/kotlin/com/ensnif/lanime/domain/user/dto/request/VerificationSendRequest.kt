package com.ensnif.lanime.domain.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class VerificationSendRequest(
    @field:Email @field:NotBlank val email: String
)