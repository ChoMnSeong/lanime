package com.ensnif.lanime.domain.animation.dto.response

data class AnimationReviewRatingsResponse(
    val averageRating: Double,
    val ratingCounts: List<RatingCountItemResponse>,
    val reviews: List<ReviewItemResponse>,
    val totalCount: Long
)

data class RatingCountItemResponse(
    val rating: Double,
    val count: Long
)

data class ReviewItemResponse(
    val reviewId: String,
    val profileId: String,
    val rating: Double,
    val comment: String,
    val createdAt: String,
    val updateAt: String,
    val profileName: String,
    val avatarURL: String
)
