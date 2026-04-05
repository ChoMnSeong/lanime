package com.ensnif.lanime.domain.episode.dto

import com.ensnif.lanime.domain.episode.entity.EncodingStatus
import java.util.UUID

data class EpisodeResponse(
    val episodeId: UUID,
    val episodeNumber: Int,
    val title: String,
    val thumbnailUrl: String?,
    val description: String?,
    val videoUrl: String?,
    val duration: Int?,
    val hlsPath: String?,
    val encodingStatus: EncodingStatus,
    val lastWatchedSecond: Int = 0,
    val isFinished: Boolean = false
)
