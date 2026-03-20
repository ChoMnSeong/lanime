package com.ensnif.lanime.domain.interaction.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("user_animation_favorite")
data class UserAnimationFavorite(
    @Id val favoriteId: UUID? = null,
    val profileId: UUID,        // ManyToOne: UserProfile 참조
    val animationId: UUID       // ManyToOne: Animation 참조
) : BaseEntity()