package net.primal.domain.notifications

enum class NotificationType(
    val id: String,
    val type: Int,
    val collapsable: Boolean,
) {
    NEW_USER_FOLLOWED_YOU(
        id = "NEW_USER_FOLLOWED_YOU",
        type = 1,
        collapsable = true,
    ),

    YOUR_POST_WAS_ZAPPED(
        id = "YOUR_POST_WAS_ZAPPED",
        type = 3,
        collapsable = true,
    ),

    YOUR_POST_WAS_LIKED(
        id = "YOUR_POST_WAS_LIKED",
        type = 4,
        collapsable = true,
    ),

    YOUR_POST_WAS_REPOSTED(
        id = "YOUR_POST_WAS_REPOSTED",
        type = 5,
        collapsable = true,
    ),

    YOUR_POST_WAS_REPLIED_TO(
        id = "YOUR_POST_WAS_REPLIED_TO",
        type = 6,
        collapsable = false,
    ),

    YOU_WERE_MENTIONED_IN_POST(
        id = "YOU_WERE_MENTIONED_IN_POST",
        type = 7,
        collapsable = false,
    ),

    YOUR_POST_WAS_MENTIONED_IN_POST(
        id = "YOUR_POST_WAS_MENTIONED_IN_POST",
        type = 8,
        collapsable = false,
    ),

    POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED(
        id = "POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED",
        type = 101,
        collapsable = true,
    ),
    POST_YOU_WERE_MENTIONED_IN_WAS_LIKED(
        id = "POST_YOU_WERE_MENTIONED_IN_WAS_LIKED",
        type = 102,
        collapsable = true,
    ),
    POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED(
        id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED",
        type = 103,
        collapsable = true,
    ),
    POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO(
        id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO",
        type = 104,
        collapsable = false,
    ),

    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED(
        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED",
        type = 201,
        collapsable = true,
    ),
    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED(
        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED",
        type = 202,
        collapsable = true,
    ),
    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED(
        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED",
        type = 203,
        collapsable = true,
    ),
    POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO(
        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO",
        type = 204,
        collapsable = false,
    ),
    YOUR_POST_WAS_HIGHLIGHTED(
        id = "YOUR_POST_WAS_HIGHLIGHTED",
        type = 301,
        collapsable = true,
    ),
    YOUR_POST_WAS_BOOKMARKED(
        id = "YOUR_POST_WAS_BOOKMARKED",
        type = 302,
        collapsable = true,
    ),
    ;

    companion object {
        fun valueOf(type: Int): NotificationType? = enumValues<NotificationType>().find { it.type == type }

        fun valueOf(id: String): NotificationType? = enumValues<NotificationType>().find { it.id == id }
    }
}
