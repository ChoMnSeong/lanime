package com.ensnif.lanime.domain.episode.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("episode_translation")
data class EpisodeTranslation(
    @Id val episodeId: UUID,
    val locale: String,
    val title: String,
    val description: String? = null
)
