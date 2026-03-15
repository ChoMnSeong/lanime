package com.ensnif.lanime.global.common

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

abstract class BaseResponse(
    open val success: Boolean,
    
    // 이 어노테이션이 배열을 "2026-03-15T10:36:57" 형태의 문자열로 바꿔줍니다.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    open val timestamp: LocalDateTime = LocalDateTime.now()
)