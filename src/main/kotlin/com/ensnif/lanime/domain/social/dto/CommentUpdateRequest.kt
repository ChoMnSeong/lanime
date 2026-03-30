package com.ensnif.lanime.domain.social.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CommentUpdateRequest(
    @field:NotBlank(message = "내용은 필수입니다.")
    @field:Size(max = 500, message = "댓글은 500자 이하로 입력해주세요.")
    val content: String
)
