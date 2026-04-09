package com.ensnif.lanime.domain.interaction.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.UUID

data class WatchedEpisodeResponse(
    val episodeId: UUID,
    val episodeNumber: Int,
    val title: String,
    val thumbnailUrl: String?,
    val duration: Int?,
    val animationId: UUID,
    val animationTitle: String,
    val lastWatchedSecond: Int,
    @JsonProperty("isFinished") val isFinished: Boolean,
    val watchedAt: LocalDateTime
)
