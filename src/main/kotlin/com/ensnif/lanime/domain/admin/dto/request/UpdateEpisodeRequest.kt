package com.ensnif.lanime.domain.admin.dto.request

data class UpdateEpisodeRequest(
    val episodeNumber: Int?,
    val title: String?,
    val thumbnailUrl: String?,
    val description: String?,
    val duration: Int?
)
