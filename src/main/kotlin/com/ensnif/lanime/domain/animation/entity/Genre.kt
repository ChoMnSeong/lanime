package com.ensnif.lanime.domain.animation.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("genre")
data class Genre(
    @Id
    val genreId: UUID? = null,
    val name: String
) : BaseEntity()