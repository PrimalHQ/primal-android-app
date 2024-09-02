package net.primal.android.feeds.repository

data class DvmFeed(
    val dvmPubkey: String,
    val dvmId: String,
    val avatarUrl: String,
    val title: String,
    val description: String,
    val amountInSats: String,
    val primalSubscriptionRequired: Boolean,
    val totalLikes: Long?,
    val totalSatsZapped: Long?,
    val isPaid: Boolean = amountInSats != "free" || primalSubscriptionRequired,
)

fun DvmFeed.buildSpec(specKind: String): String =
    "{\"dvm_id\":\"$dvmId\",\"dvm_pubkey\":\"$dvmPubkey\",\"kind\":\"$specKind\"}"
