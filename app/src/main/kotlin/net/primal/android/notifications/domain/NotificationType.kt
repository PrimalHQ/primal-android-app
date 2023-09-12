package net.primal.android.notifications.domain

enum class NotificationType(val type: Int) {
    NEW_USER_FOLLOWED_YOU(type = 1),
    USER_UNFOLLOWED_YOU(type = 2),

    YOUR_POST_WAS_ZAPPED(type = 3),
    YOUR_POST_WAS_LIKED(type = 4),
    YOUR_POST_WAS_REPOSTED(type = 5),
    YOUR_POST_WAS_REPLIED_TO(type = 6),

    YOU_WERE_MENTIONED_IN_POST(type = 7),
    YOUR_POST_WAS_MENTIONED_IN_POST(type = 8),

    POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED(type = 101),
    POST_YOU_WERE_MENTIONED_IN_WAS_LIKED(type = 102),
    POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED(type = 103),
    POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO(type = 104),

    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED(type = 201),
    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED(type = 202),
    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED(type = 203),
    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO(type = 204);

    companion object {
        fun valueOf(type: Int): NotificationType? =
            enumValues<NotificationType>().find { it.type == type }
    }
}
