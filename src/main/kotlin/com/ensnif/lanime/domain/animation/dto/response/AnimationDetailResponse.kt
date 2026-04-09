package com.ensnif.lanime.domain.animation.dto.response

import com.ensnif.lanime.domain.animation.entity.AnimationStatus
import com.fasterxml.jackson.annotation.JsonProperty

data class AnimationDetailResponse(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val type: String,
    val genres: List<String>,
    val ageRating: String,
    val status: AnimationStatus,
    @JsonProperty("isFavorite") val isFavorite: Boolean = false
)
