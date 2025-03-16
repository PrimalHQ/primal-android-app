package net.primal.android.settings.api.model

import kotlinx.serialization.Serializable
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEvent

@Serializable
data class GetAppSettingsResponse(
    val userSettings: NostrEvent? = null,
    val defaultSettings: PrimalEvent? = null,
)
