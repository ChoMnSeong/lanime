package com.ensnif.lanime.domain.interaction.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("favorite")
data class Favorite(
    @Id
    val favoriteId: UUID? = null,
    val profileId: UUID,    // UserProfile 참조
    val animationId: UUID   // Animation 참조
) : BaseEntity()