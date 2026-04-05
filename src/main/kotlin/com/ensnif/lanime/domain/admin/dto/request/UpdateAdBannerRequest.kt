package com.ensnif.lanime.domain.admin.dto.request

import java.time.LocalDateTime

data class UpdateAdBannerRequest(
    val title: String?,
    val imageUrl: String?,
    val logoImageUrl: String?,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
    val isActive: Boolean?
)
