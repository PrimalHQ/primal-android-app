package net.primal.domain.feeds

data class DvmFeed(
    val eventId: String,
    val dvmPubkey: String,
    val dvmId: String,
    val dvmLnUrlDecoded: String?,
    val title: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val amountInSats: String? = null,
    val primalSpec: String? = null,
    val primalSubscriptionRequired: Boolean? = null,
    val isPaid: Boolean = amountInSats != null && amountInSats != "free" || primalSubscriptionRequired == true,
    val kind: FeedSpecKind? = null,
    val isPrimalFeed: Boolean? = null,
    val actionUserIds: List<String> = emptyList(),
)

fun DvmFeed.buildSpec(specKind: FeedSpecKind): String {
    return primalSpec ?: "{\"dvm_id\":\"$dvmId\",\"dvm_pubkey\":\"$dvmPubkey\",\"kind\":\"${specKind.id}\"}"
}
