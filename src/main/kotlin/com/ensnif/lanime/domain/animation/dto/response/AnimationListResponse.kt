package com.ensnif.lanime.domain.animation.dto.response

import com.ensnif.lanime.domain.animation.entity.AnimationStatus
import java.time.LocalDate

data class AnimationListResponse(
    val id: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val type: String,
    val genres: List<String>,
    val ageRating: String,
    val status: AnimationStatus,
    val airDay: String?,
    val releasedAt: LocalDate?
)
