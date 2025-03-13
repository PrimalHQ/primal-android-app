package net.primal.android.events.reactions

import net.primal.android.core.compose.profile.model.ProfileDetailsUi

data class EventActionUi(
    val profile: ProfileDetailsUi,
    val action: String,
)
