package com.ensnif.lanime.domain.animation.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("animation_translation")
data class AnimationTranslation(
    @Id val animationId: UUID,
    val locale: String,
    val title: String,
    val description: String? = null
)
