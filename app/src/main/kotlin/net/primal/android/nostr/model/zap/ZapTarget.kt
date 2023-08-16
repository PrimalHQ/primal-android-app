package net.primal.android.nostr.model.zap

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray

sealed class ZapTarget {
    data class Profile(val pubkey: String) : ZapTarget()
    data class Note(val id: String, val authorPubkey: String) : ZapTarget()
}
