package com.ensnif.lanime.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class AdminResetPasswordRequest(
    @field:NotBlank(message = "현재 비밀번호는 필수입니다.")
    val currentPassword: String,

    @field:NotBlank(message = "새 비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
    )
    val newPassword: String
)
