package net.primal.android.settings.muted.list.model

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.legend.domain.LegendaryCustomization

data class MutedUserUi(
    val userId: String,
    val displayName: String,
    val avatarCdnImage: CdnImage? = null,
    val internetIdentifier: String? = null,
    val legendaryCustomization: LegendaryCustomization? = null,
)
