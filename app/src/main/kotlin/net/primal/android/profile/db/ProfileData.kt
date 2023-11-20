package net.primal.android.profile.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.android.attachments.domain.CdnResourceVariant

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
    val lnUrl: String? = null,
    val avatarUrl: String? = null,
    val avatarVariants: List<CdnResourceVariant> = emptyList(),
    val bannerUrl: String? = null,
    val bannerVariants: List<CdnResourceVariant> = emptyList(),
    val website: String? = null,
    val about: String? = null,
)
