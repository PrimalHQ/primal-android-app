package net.primal.android.settings.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.primal.PrimalEvent

@Serializable
data class AppSettingsResponse(
    val defaultSettings: PrimalEvent? = null,
)
