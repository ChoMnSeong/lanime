package com.ensnif.lanime.domain.user.dto.request

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:NotBlank(message = "refresh token은 필수입니다.")
    val refreshToken: String
)
