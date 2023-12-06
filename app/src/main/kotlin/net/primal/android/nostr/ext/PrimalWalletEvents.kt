package net.primal.android.nostr.ext

import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.nostr.model.primal.PrimalEvent
import net.primal.android.nostr.model.primal.content.WalletBalanceContent

fun PrimalEvent.asWalletBalanceInBtcOrNull(): Double? {
    val balance = NostrJson.decodeFromStringOrNull<WalletBalanceContent>(this.content)
    return balance?.amount?.toDoubleOrNull()
}
