package com.ensnif.lanime.domain.admin.dto.response

import java.util.UUID

data class ImportAnimationResponse(
    val animationId: UUID,
    val title: String,
    val episodesImported: Int,
    val translationsCreated: List<String>
)
