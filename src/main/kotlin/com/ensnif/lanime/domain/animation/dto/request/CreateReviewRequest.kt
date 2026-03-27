package com.ensnif.lanime.domain.animation.dto.request

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull

data class CreateReviewRequest(

    @field:NotNull(message = "평점은 필수입니다.")
    @field:DecimalMin(value = "0.5", message = "평점은 0.5점 이상이어야 합니다.")
    @field:DecimalMax(value = "5.0", message = "평점은 5.0점 이하이어야 합니다.")
    val rating: Double,

    val comment: String? = null
)
