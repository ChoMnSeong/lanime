package com.ensnif.lanime.domain.user.dto.request

import jakarta.validation.constraints.NotBlank

data class ResetPinRequest(
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String
)
