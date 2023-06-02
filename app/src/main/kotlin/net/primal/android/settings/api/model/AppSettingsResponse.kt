package net.primal.android.settings.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.primal.NostrPrimalEvent

@Serializable
data class AppSettingsResponse(
    val event: NostrPrimalEvent? = null,
)
