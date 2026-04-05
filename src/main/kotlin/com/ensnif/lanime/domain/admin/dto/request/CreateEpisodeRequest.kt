package com.ensnif.lanime.domain.admin.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateEpisodeRequest(
    @field:NotNull val episodeNumber: Int,
    @field:NotBlank val title: String,
    val thumbnailUrl: String?,
    val description: String?,
    val duration: Int?
)
