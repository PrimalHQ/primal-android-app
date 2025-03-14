package net.primal.domain

data class EventZap(
    val id: String,
    val zapperId: String,
    val zapperName: String,
    val zapperHandle: String,
    val zappedAt: Long,
    val message: String?,
    val amountInSats: ULong,
    val zapperInternetIdentifier: String? = null,
    val zapperAvatarCdnImage: CdnImage? = null,
    val zapperLegendProfile: PrimalLegendProfile? = null,
) {
    companion object {
        val DefaultComparator = compareByDescending<EventZap> { it.amountInSats }.thenBy { it.zappedAt }
    }
}
