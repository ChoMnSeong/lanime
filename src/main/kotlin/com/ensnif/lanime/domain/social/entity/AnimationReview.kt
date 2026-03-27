package com.ensnif.lanime.domain.social.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("animation_review")
data class AnimationReview(
    @Id val reviewId: UUID? = null,
    val animationId: UUID,
    val profileId: UUID,
    val score: Double,
    val content: String?
) : BaseEntity()
