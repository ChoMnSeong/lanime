package com.ensnif.lanime.global.common

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    override val success: Boolean = true,
    val message: String? = null,
    val data: T? = null
) : BaseResponse(success) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(data = data)
        fun success(message: String): ApiResponse<Unit> = ApiResponse(message = message)
    }
}