package com.ensnif.lanime.domain.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class AdminUpdateEmailRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val newEmail: String,

    @field:NotBlank(message = "인증 코드는 필수입니다.")
    val verificationCode: String
)
