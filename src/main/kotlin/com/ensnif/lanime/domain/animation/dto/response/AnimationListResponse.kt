package com.ensnif.lanime.domain.animation.dto.response

data class AnimationListResponse(
    val id: String,
    val title: String,
    val thumbnailURL: String,
    val type: String,
    val ageRating: String
)
