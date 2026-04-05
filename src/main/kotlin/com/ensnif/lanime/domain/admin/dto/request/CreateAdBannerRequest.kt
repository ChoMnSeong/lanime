package com.ensnif.lanime.domain.admin.dto.request

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class CreateAdBannerRequest(
    @field:NotBlank val title: String,
    @field:NotBlank val imageUrl: String,
    val logoImageUrl: String?,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
    val isActive: Boolean = true
)
