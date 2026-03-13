package com.ensnif.lanime.domain.animation.entity

import com.ensnif.lanime.global.common.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("genre")
data class Genre(
    @Id
    val genreId: UUID? = null,
    val name: String,   // 장르 명칭 (예: 판타지)
    val slug: String    // URL용 영문 명칭 (예: fantasy)
) : BaseEntity()