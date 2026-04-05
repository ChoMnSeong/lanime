package com.ensnif.lanime.domain.user.dto.response

import com.ensnif.lanime.domain.user.entity.UserProfile
import java.util.UUID

data class ProfileInfoResponse(
    val profileId: UUID,
    val name: String,
    val avatarUrl: String?,
    val isOwner: Boolean
) {
    companion object {
        fun from(profile: UserProfile) = ProfileInfoResponse(
            profileId = profile.profileId!!,
            name = profile.name,
            avatarUrl = profile.avatarUrl,
            isOwner = profile.isOwner
        )
    }
}
