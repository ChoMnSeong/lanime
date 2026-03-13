package com.ensnif.lanime.domain.user.dto.response

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresIn: Long,
    val tokenType: String = "Bearer"
)