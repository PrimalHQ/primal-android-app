package net.primal.android.drawer.multiaccount.model

import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.domain.UserAccount
import net.primal.domain.links.CdnImage

data class UserAccountUi(
    val pubkey: String,
    val displayName: String,
    val internetIdentifier: String? = null,
    val avatarCdnImage: CdnImage? = null,
    val legendaryCustomization: LegendaryCustomization? = null,
    val lastAccessedAt: Long,
)

fun UserAccount.asUserAccountUi() =
    UserAccountUi(
        pubkey = pubkey,
        displayName = authorNameUiFriendly(),
        internetIdentifier = internetIdentifier,
        avatarCdnImage = avatarCdnImage,
        legendaryCustomization = primalLegendProfile?.asLegendaryCustomization(),
        lastAccessedAt = lastAccessedAt,
    )
