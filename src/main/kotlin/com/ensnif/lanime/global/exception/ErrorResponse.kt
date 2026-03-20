package com.ensnif.lanime.global.exception

import com.ensnif.lanime.global.common.dto.BaseResponse
import com.fasterxml.jackson.annotation.JsonInclude

data class ErrorResponse(
    override val success: Boolean = false,
    val status: Int,
    val code: String,
    val message: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val errors: List<FieldError>? = null
) : BaseResponse(success) {
    data class FieldError(
        val field: String,
        val value: String,
        val reason: String
    )
}