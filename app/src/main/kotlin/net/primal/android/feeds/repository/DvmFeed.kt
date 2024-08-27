package net.primal.android.feeds.repository

data class DvmFeed(
    val dvmPubkey: String,
    val dvmId: String,
    val dvmSpec: String = "dvm:$dvmPubkey;$dvmId",
    val avatarUrl: String,
    val title: String,
    val description: String,
    val amountInSats: String,
    val primalSubscriptionRequired: Boolean,
    val totalLikes: Long?,
    val totalSatsZapped: Long?,
    val isPaid: Boolean = amountInSats != "free" || primalSubscriptionRequired,
)
