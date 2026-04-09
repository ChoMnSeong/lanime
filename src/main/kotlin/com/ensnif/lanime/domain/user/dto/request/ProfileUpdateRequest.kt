package com.ensnif.lanime.domain.user.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Max

data class ProfileUpdateRequest(
    val name: String?,
    val avatarUrl: String?,
    val pin: String?, // 새로운 PIN (4~6자리 숫자 문자열)
    @field:Min(value = 1, message = "나이는 1 이상이어야 합니다.")
    @field:Max(value = 150, message = "나이는 150 이하여야 합니다.")
    val age: Int? = null,
)