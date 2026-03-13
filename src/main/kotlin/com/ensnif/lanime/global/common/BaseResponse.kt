package com.ensnif.lanime.global.common

import java.time.LocalDateTime

abstract class BaseResponse(
    open val success: Boolean,
    open val timestamp: LocalDateTime = LocalDateTime.now()
)