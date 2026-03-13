package com.ensnif.lanime.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * 프로필 PIN 검증 요청 DTO
 */
data class ProfilePinRequest(
    @field:NotBlank(message = "PIN 번호를 입력해주세요.")
    @field:Pattern(regexp = "^[0-9]{4,6}$", message = "PIN 번호는 4~6자리의 숫자여야 합니다.")
    val pin: String
)