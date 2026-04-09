package com.ensnif.lanime.domain.admin.dto.request

import jakarta.validation.constraints.Positive

data class ImportAnimationRequest(
    @field:Positive val malId: Int
)
