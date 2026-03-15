package com.ensnif.lanime.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * 프로필 PIN 검증 요청 DTO
 */
data class ProfileCreateRequest(

    @field:NotBlank(message = "프로필 이미지는 필수입니다.")
    val avatarUrl: String,

    @field:NotBlank(message = "PIN 번호를 입력해주세요.")
    @field:Pattern(regexp = "^[0-9]{4,6}$", message = "PIN 번호는 4~6자리의 숫자여야 합니다.")
    val pin: String? = null,

    @field:NotBlank(message = "닉네임은 필수입니다.")
    @field:Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
    val nickname: String,
)