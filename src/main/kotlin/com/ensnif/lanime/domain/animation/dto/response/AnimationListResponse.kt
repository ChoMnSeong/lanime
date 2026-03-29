package com.ensnif.lanime.domain.animation.dto.response

data class AnimationListResponse(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val type: String,
    val ageRating: String
)
