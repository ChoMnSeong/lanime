package com.ensnif.lanime.domain.user.dto.request

data class ProfileUpdateRequest(
    val name: String?,
    val avatarUrl: String?,
    val pin: String? // 새로운 PIN (4~6자리 숫자 문자열)
)