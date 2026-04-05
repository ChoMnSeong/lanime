package com.ensnif.lanime.domain.admin.dto.response

data class AdminSigninResponse(
    val accessToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer"
)
