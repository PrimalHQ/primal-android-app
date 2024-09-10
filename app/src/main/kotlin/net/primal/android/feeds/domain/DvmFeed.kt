package net.primal.android.feeds.domain

data class DvmFeed(
    val dvmPubkey: String,
    val dvmId: String,
    val title: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val amountInSats: String? = null,
    val primalSubscriptionRequired: Boolean? = null,
    val totalLikes: Long? = null,
    val totalSatsZapped: Long? = null,
    val isPaid: Boolean = amountInSats != null && amountInSats != "free" || primalSubscriptionRequired == true,
)

fun DvmFeed.buildSpec(specKind: FeedSpecKind): String =
    "{\"dvm_id\":\"$dvmId\",\"dvm_pubkey\":\"$dvmPubkey\",\"kind\":\"${specKind.id}\"}"
