package net.primal.android.premium.legend.model

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.legend.domain.LegendaryCustomization

data class LegendLeaderboardEntry(
    val userId: String,
    val avatarCdnImage: CdnImage?,
    val displayName: String?,
    val internetIdentifier: String?,
    val legendaryCustomization: LegendaryCustomization?,
    val donatedBtc: Double,
)
