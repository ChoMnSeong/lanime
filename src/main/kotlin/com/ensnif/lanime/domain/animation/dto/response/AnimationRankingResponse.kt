package com.ensnif.lanime.domain.animation.dto.response

data class AnimationRankingResponse(
    val rank: Long,
    val id: String,
    val title: String,
    val thumbnailURL: String,
    val type: String,
    val ageRating: String,
    val averageScore: Double,
    val reviewCount: Long
)
