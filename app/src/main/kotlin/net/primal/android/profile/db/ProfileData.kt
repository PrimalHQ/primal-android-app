package net.primal.android.profile.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.CdnImage
import net.primal.domain.PrimalPremiumInfo

@Entity
data class ProfileData(
    @PrimaryKey
    val ownerId: String,
    val eventId: String,
    val createdAt: Long,
    val raw: String,
    val handle: String? = null,
    val displayName: String? = null,
    val internetIdentifier: String? = null,
    val lightningAddress: String? = null,
    val lnUrlDecoded: String? = null,
    val avatarCdnImage: CdnImage? = null,
    val bannerCdnImage: CdnImage? = null,
    val website: String? = null,
    val about: String? = null,
    val aboutUris: List<String> = emptyList(),
    val aboutHashtags: List<String> = emptyList(),
    val primalName: String? = null,
    val primalPremiumInfo: PrimalPremiumInfo? = null,
    val blossoms: List<String> = emptyList(),
)
