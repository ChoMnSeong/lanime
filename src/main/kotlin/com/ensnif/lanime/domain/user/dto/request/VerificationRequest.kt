package com.ensnif.lanime.domain.user.dto.request

data class VerificationRequest(
    val email: String,
    val code: String
)