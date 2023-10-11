package net.primal.android.settings.muted.repository

import kotlinx.serialization.json.JsonArray
import net.primal.android.settings.muted.db.MutedAccount

fun String.asMutedPO(): MutedAccount = MutedAccount(pubkey = this)

fun List<JsonArray>?.mapToPubkeySet(): Set<String>? {
    return this?.filter { it.size == 2 }?.map { it[1].toString() }?.toSet()
}
