package net.primal.android.notifications.domain

enum class NotificationType(
    val id: String,
    val type: Int,
    val section: NotificationSection,
    val collapsable: Boolean,
) {
    NEW_USER_FOLLOWED_YOU(
        id = "NEW_USER_FOLLOWED_YOU",
        type = 1,
        section = NotificationSection.CORE_NOTIFICATIONS,
        collapsable = true,
    ),

    YOUR_POST_WAS_ZAPPED(
        id = "YOUR_POST_WAS_ZAPPED",
        type = 3,
        section = NotificationSection.CORE_NOTIFICATIONS,
        collapsable = true,
    ),

    YOUR_POST_WAS_LIKED(
        id = "YOUR_POST_WAS_LIKED",
        type = 4,
        section = NotificationSection.CORE_NOTIFICATIONS,
        collapsable = true,
    ),

    YOUR_POST_WAS_REPOSTED(
        id = "YOUR_POST_WAS_REPOSTED",
        type = 5,
        section = NotificationSection.CORE_NOTIFICATIONS,
        collapsable = true,
    ),

    YOUR_POST_WAS_REPLIED_TO(
        id = "YOUR_POST_WAS_REPLIED_TO",
        type = 6,
        section = NotificationSection.CORE_NOTIFICATIONS,
        collapsable = false,
    ),

    YOU_WERE_MENTIONED_IN_POST(
        id = "YOU_WERE_MENTIONED_IN_POST",
        type = 7,
        section = NotificationSection.CORE_NOTIFICATIONS,
        collapsable = false,
    ),

    YOUR_POST_WAS_MENTIONED_IN_POST(
        id = "YOUR_POST_WAS_MENTIONED_IN_POST",
        type = 8,
        section = NotificationSection.CORE_NOTIFICATIONS,
        collapsable = false,
    ),

    POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED(
        id = "POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED",
        type = 101,
        section = NotificationSection.NOTE_YOU_WERE_MENTIONED_IN,
        collapsable = true,
    ),
    POST_YOU_WERE_MENTIONED_IN_WAS_LIKED(
        id = "POST_YOU_WERE_MENTIONED_IN_WAS_LIKED",
        type = 102,
        section = NotificationSection.NOTE_YOU_WERE_MENTIONED_IN,
        collapsable = true,
    ),
    POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED(
        id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED",
        type = 103,
        section = NotificationSection.NOTE_YOU_WERE_MENTIONED_IN,
        collapsable = true,
    ),
    POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO(
        id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO",
        type = 104,
        section = NotificationSection.NOTE_YOU_WERE_MENTIONED_IN,
        collapsable = false,
    ),

    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED(
        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED",
        type = 201,
        section = NotificationSection.NOTE_YOUR_NOTE_WAS_MENTIONED_IN,
        collapsable = true,
    ),
    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED(
        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED",
        type = 202,
        section = NotificationSection.NOTE_YOUR_NOTE_WAS_MENTIONED_IN,
        collapsable = true,
    ),
    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED(
        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED",
        type = 203,
        section = NotificationSection.NOTE_YOUR_NOTE_WAS_MENTIONED_IN,
        collapsable = true,
    ),
    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO(
        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO",
        type = 204,
        section = NotificationSection.NOTE_YOUR_NOTE_WAS_MENTIONED_IN,
        collapsable = false,
    ),
    ;

    companion object {
        fun valueOf(type: Int): NotificationType? = enumValues<NotificationType>().find { it.type == type }

        fun valueOf(id: String): NotificationType? = enumValues<NotificationType>().find { it.id == id }
    }
}
