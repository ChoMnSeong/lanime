package com.ensnif.lanime.domain.animation.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("animation_genre")
data class AnimationGenre(
    @Id
    val animationGenreId: UUID? = null,
    val animationId: UUID, // Animation ID 참조
    val genreId: UUID     // Genre ID 참조
)