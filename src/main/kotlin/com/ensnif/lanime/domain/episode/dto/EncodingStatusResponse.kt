package com.ensnif.lanime.domain.episode.dto

import com.ensnif.lanime.domain.episode.entity.EncodingStatus
import java.time.LocalDateTime
import java.util.UUID

data class EncodingStatusResponse(
    val episodeId: UUID,
    val jobId: UUID,
    val status: EncodingStatus,
    val errorMessage: String?,
    val startedAt: LocalDateTime?,
    val completedAt: LocalDateTime?
)
