package net.primal.data.remote.api.notifications.model

import net.primal.domain.notifications.NotificationGroup

val NotificationGroup.wireToken: String?
    get() = when (this) {
        NotificationGroup.ALL -> null
        NotificationGroup.ZAPS -> "zaps"
        NotificationGroup.REPLIES -> "replies"
        NotificationGroup.MENTIONS -> "mentions"
        NotificationGroup.REPOSTS -> "reposts"
    }
