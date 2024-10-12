package net.primal.android.feeds.domain

import net.primal.android.profile.db.ProfileData

data class DvmFeed(
    val eventId: String,
    val dvmPubkey: String,
    val dvmId: String,
    val lnUrlDecoded: String?,
    val title: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val amountInSats: String? = null,
    val primalSubscriptionRequired: Boolean? = null,
    val totalLikes: Long? = null,
    val totalSatsZapped: Long? = null,
    val isPaid: Boolean = amountInSats != null && amountInSats != "free" || primalSubscriptionRequired == true,
    val kind: FeedSpecKind? = null,
    val isPrimal: Boolean? = null,
    val followsActions: List<ProfileData> = emptyList(),
    val userLiked: Boolean? = false,
    val userZapped: Boolean? = false,
)

fun DvmFeed.buildSpec(specKind: FeedSpecKind): String =
    "{\"dvm_id\":\"$dvmId\",\"dvm_pubkey\":\"$dvmPubkey\",\"kind\":\"${specKind.id}\"}"

