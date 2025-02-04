package net.primal.android.premium.leaderboard.domain

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.profile.details.ui.model.PremiumProfileDataUi

data class LeaderboardLegendEntry(
    val userId: String,
    val avatarCdnImage: CdnImage?,
    val displayName: String?,
    val internetIdentifier: String?,
    val premiumProfileDataUi: PremiumProfileDataUi?,
    val donatedBtc: Double,
)
