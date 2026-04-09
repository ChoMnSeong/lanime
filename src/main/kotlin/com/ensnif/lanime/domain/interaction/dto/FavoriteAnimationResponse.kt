package com.ensnif.lanime.domain.interaction.dto

import java.time.LocalDateTime
import java.util.UUID

data class FavoriteAnimationResponse(
    val animationId: UUID,
    val title: String,
    val thumbnailUrl: String?,
    val type: String,
    val status: String,
    val favoritedAt: LocalDateTime
)
