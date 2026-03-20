package com.ensnif.lanime.global.common.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    override val success: Boolean = true,
    val message: String? = null,
    val data: T? = null
) : BaseResponse(success) {
    companion object {
        // 데이터를 담을 때
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(data = data)
        
        // 메시지만 담을 때 (이름을 바꿈)
        fun withMessage(message: String): ApiResponse<Unit> = ApiResponse(message = message, success = true)
    }
}