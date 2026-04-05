package com.ensnif.lanime.domain.animation.dto.response

import java.time.LocalDate
import java.util.UUID

data class AnimationListItemResponse(
    val animationId: UUID,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val type: String,
    val ageRating: String,
    val status: String,
    val airDay: String?,
    val releasedAt: LocalDate?
)
