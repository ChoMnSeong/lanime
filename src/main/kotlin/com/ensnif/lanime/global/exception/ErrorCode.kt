package com.ensnif.lanime.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val code: String, val message: String) {
    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C003", "해당 리소스에 대한 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C004", "인증 정보가 없거나 유효하지 않습니다."), // ✨ 추가됨

    // 유저 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "해당 사용자를 찾을 수 없습니다."),
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, "U002", "이미 존재하는 이메일입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "U003", "이메일 인증이 완료되지 않았습니다."),
    
    // 애니메이션 관련
    ANIMATION_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "해당 애니메이션 정보를 찾을 수 없습니다."),

    // 리뷰 관련
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "R001", "이미 리뷰를 작성하셨습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R002", "작성된 리뷰를 찾을 수 없습니다.")
}