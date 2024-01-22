package net.primal.android.nostr.model.primal.content

import kotlinx.serialization.Serializable

@Serializable
data class ContentZapConfigItem(
    val emoji: String,
    val amount: Long,
    val message: String,
)

val DEFAULT_ZAP_CONFIG = listOf(
    ContentZapConfigItem(emoji = "👍", amount = 21, message = "Great post 👍"),
    ContentZapConfigItem(emoji = "🚀", amount = 420, message = "Let's go 🚀"),
    ContentZapConfigItem(emoji = "☕", amount = 1000, message = "Coffie on me ☕"),
    ContentZapConfigItem(emoji = "🍻", amount = 5000, message = "Cheers 🍻"),
    ContentZapConfigItem(emoji = "🍷", amount = 10000, message = "Party time 🍷"),
    ContentZapConfigItem(emoji = "👑", amount = 100000, message = "Generational wealth 👑"),
)
