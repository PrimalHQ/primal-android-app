package net.primal.android.premium.legend.domain

import net.primal.android.attachments.domain.CdnImage

data class LeaderboardLegendEntry(
    val userId: String,
    val avatarCdnImage: CdnImage?,
    val displayName: String?,
    val internetIdentifier: String?,
    val legendaryCustomization: LegendaryCustomization?,
    val donatedBtc: Double,
)
