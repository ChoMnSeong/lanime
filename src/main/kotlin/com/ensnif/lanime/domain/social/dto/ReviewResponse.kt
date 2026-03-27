package com.ensnif.lanime.domain.social.dto

import java.time.LocalDateTime
import java.util.UUID

data class ReviewResponse(
    val reviewId: UUID,
    val profileId: UUID,
    val score: Double,
    val content: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val profileName: String,
    val avatarUrl: String?
)
