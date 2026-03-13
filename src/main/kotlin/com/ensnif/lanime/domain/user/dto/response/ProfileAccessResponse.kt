package com.ensnif.lanime.domain.user.dto.response

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * 프로필 선택 및 PIN 검증 결과를 담는 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON 응답에서 제외하여 깔끔하게 유지
data class ProfileAccessResponse(
    /**
     * PIN(비밀번호) 입력이 필요한지 여부
     * true: 클라이언트에서 PIN 입력 모달을 띄워야 함
     * false: PIN이 없거나 검증에 성공했으므로 즉시 진입 가능
     */
    val isPasswordRequired: Boolean,

    /**
     * 프로필 전용 영구 토큰
     * isPasswordRequired가 false일 때만 값이 담겨서 내려갑니다.
     */
    val profileToken: String? = null
)