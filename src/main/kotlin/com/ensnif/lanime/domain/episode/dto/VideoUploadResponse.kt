package com.ensnif.lanime.domain.episode.dto

import com.ensnif.lanime.domain.episode.entity.EncodingStatus
import java.util.UUID

data class VideoUploadResponse(
    val episodeId: UUID,
    val jobId: UUID,
    val encodingStatus: EncodingStatus,
    val message: String
)
