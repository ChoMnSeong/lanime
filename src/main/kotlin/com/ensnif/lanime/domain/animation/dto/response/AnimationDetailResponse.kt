package com.ensnif.lanime.domain.animation.dto.response

data class AnimationDetailResponse(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailURL: String,
    val type: String,
    val genres: List<String>,
    val ageRating: String,
    val status: String
)
