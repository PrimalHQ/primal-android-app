package net.primal.android.nostr.model

object NostrEventKindRange {
    val RegularEvents = IntRange(1000, 9999)
    val ReplaceableEvents = IntRange(10000, 19999)
    val EphemeralEvents = IntRange(20000, 29999)
    val ParameterizedReplaceableEvents = IntRange(30000, 39999)

    val PrimalEvents = IntRange(10_000_100, 10_099_999)
}
