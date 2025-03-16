package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class WalletRequestBody(
    @SerialName("operation_event") val event: NostrEvent,
)
