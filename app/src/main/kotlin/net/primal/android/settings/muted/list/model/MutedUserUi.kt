package net.primal.android.settings.muted.list.model

import net.primal.android.attachments.domain.CdnResourceVariant

data class MutedUserUi(
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val avatarVariants: List<CdnResourceVariant> = emptyList(),
    val internetIdentifier: String? = null,
)
