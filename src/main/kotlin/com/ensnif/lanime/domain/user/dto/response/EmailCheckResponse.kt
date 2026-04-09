package com.ensnif.lanime.domain.user.dto.response

/**
 * 이메일 중복 체크 응답
 * isRegistered가 true면 로그인으로, false면 회원가입(인증)으로 유도
 */
import com.fasterxml.jackson.annotation.JsonProperty

data class EmailCheckResponse(
    val email: String,
    @JsonProperty("isRegistered") val isRegistered: Boolean
)