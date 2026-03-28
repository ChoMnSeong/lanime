package com.ensnif.lanime.global.context

import java.util.UUID

data class UserProfileContext(
    val email: String,
    val profileId: UUID? = null,
    val isAdmin: Boolean = false
)