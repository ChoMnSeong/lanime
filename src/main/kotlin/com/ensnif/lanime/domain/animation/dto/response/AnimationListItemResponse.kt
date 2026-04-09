package com.ensnif.lanime.domain.animation.dto.response

import com.ensnif.lanime.domain.animation.entity.AnimationStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class AnimationListItemResponse(
    val animationId: UUID,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val type: String,
    val ageRating: String,
    val status: AnimationStatus,
    val airDay: String?,
    val releasedAt: LocalDate?,
    val createdAt: LocalDateTime? = null
)
