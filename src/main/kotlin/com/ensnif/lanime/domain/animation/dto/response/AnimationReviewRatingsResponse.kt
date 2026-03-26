package com.ensnif.lanime.domain.animation.dto.response

data class AnimationReviewRatingsResponse(
    val averageRating: Double,
    val ratingCounts: List<RatingCount>,
    val reviews: List<Review>,
    val totalCount: Long
)

data class RatingCount(
    val rating: Int,
    val count: Long
)

data class Review(
    val reviewId: String,
    val profileId: String,
    val rating: Int,
    val comment: String,
    val createdAt: String,
    val updateAt: String,
    val profileName: String,
    val avatarURL: String
)
