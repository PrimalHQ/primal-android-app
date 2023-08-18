package net.primal.android.settings.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent

@Serializable
data class SetAppSettingsRequest(
    @SerialName("settings_event") val settingsEvent: NostrEvent,
)
