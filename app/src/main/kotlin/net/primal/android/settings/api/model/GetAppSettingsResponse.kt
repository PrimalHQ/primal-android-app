package net.primal.android.settings.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

@Serializable
data class GetAppSettingsResponse(
    val userSettings: NostrEvent? = null,
    val defaultSettings: PrimalEvent? = null,
)
