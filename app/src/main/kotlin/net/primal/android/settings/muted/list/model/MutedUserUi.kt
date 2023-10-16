package net.primal.android.settings.muted.list.model

import net.primal.android.core.compose.media.model.MediaResourceUi

data class MutedUserUi(
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val internetIdentifier: String? = null,
    val profileResources: List<MediaResourceUi> = emptyList(),
)
