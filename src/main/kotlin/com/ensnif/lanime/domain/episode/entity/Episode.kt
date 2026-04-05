package com.ensnif.lanime.domain.episode.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("episode")
data class Episode(
    @Id val episodeId: UUID? = null,
    val animationId: UUID,
    val episodeNumber: Int,
    val title: String,
    val thumbnailUrl: String?,
    val description: String?,
    val videoUrl: String?,
    val duration: Int?,
    val hlsPath: String? = null,
    val encodingStatus: EncodingStatus = EncodingStatus.PENDING
) : BaseEntity()
