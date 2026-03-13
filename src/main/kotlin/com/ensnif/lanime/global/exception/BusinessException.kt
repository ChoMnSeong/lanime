package com.ensnif.lanime.global.exception

open class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)