package net.primal.domain.notifications

import net.primal.domain.notifications.NotificationType.NEW_USER_FOLLOWED_YOU
import net.primal.domain.notifications.NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED
import net.primal.domain.notifications.NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO
import net.primal.domain.notifications.NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED
import net.primal.domain.notifications.NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED
import net.primal.domain.notifications.NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED
import net.primal.domain.notifications.NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO
import net.primal.domain.notifications.NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED
import net.primal.domain.notifications.NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED
import net.primal.domain.notifications.NotificationType.YOUR_POST_WAS_BOOKMARKED
import net.primal.domain.notifications.NotificationType.YOUR_POST_WAS_HIGHLIGHTED
import net.primal.domain.notifications.NotificationType.YOUR_POST_WAS_LIKED
import net.primal.domain.notifications.NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST
import net.primal.domain.notifications.NotificationType.YOUR_POST_WAS_REPLIED_TO
import net.primal.domain.notifications.NotificationType.YOUR_POST_WAS_REPOSTED
import net.primal.domain.notifications.NotificationType.YOUR_POST_WAS_ZAPPED
import net.primal.domain.notifications.NotificationType.YOU_WERE_MENTIONED_IN_POST

sealed class NotificationSettingsType(val id: String, val order: Int) {

    sealed class PushNotifications(id: String, order: Int) : NotificationSettingsType(id = id, order = order) {
        data object NewFollows : PushNotifications(id = NEW_FOLLOWS, order = 1)
        data object Zaps : PushNotifications(id = ZAPS, order = 2)
        data object Reactions : PushNotifications(id = REACTIONS, order = 3)
        data object Replies : PushNotifications(id = REPLIES, order = 4)
        data object Reposts : PushNotifications(id = REPOSTS, order = 5)
        data object Mentions : PushNotifications(id = MENTIONS, order = 6)
        data object DirectMessages : PushNotifications(id = DIRECT_MESSAGES, order = 7)
        data object WalletTransactions : PushNotifications(id = WALLET_TRANSACTIONS, order = 8)

        companion object {
            private const val NEW_FOLLOWS = "NEW_FOLLOWS"
            private const val ZAPS = "ZAPS"
            private const val REACTIONS = "REACTIONS"
            private const val REPLIES = "REPLIES"
            private const val REPOSTS = "REPOSTS"
            private const val MENTIONS = "MENTIONS"
            private const val DIRECT_MESSAGES = "DIRECT_MESSAGES"
            private const val WALLET_TRANSACTIONS = "WALLET_TRANSACTIONS"

            fun valueOf(id: String): PushNotifications? {
                return when (id) {
                    NEW_FOLLOWS -> NewFollows
                    ZAPS -> Zaps
                    REACTIONS -> Reactions
                    REPLIES -> Replies
                    REPOSTS -> Reposts
                    MENTIONS -> Mentions
                    DIRECT_MESSAGES -> DirectMessages
                    WALLET_TRANSACTIONS -> WalletTransactions
                    else -> null
                }
            }
        }
    }

    sealed class Preferences(id: String, order: Int) : NotificationSettingsType(id = id, order = order) {
        data object HellThread : Preferences(id = HELL_THREAD, order = 1)
        data object DMsFromFollows : Preferences(id = DMS_FROM_FOLLOWS, order = 2)
        data object ReactionsFromFollows : Preferences(id = REACTIONS_FROM_FOLLOWS, order = 3)
        data object NotificationAmountThreshold : Preferences(id = NOTIFICATION_AMOUNT_THRESHOLD, order = 4)

        companion object {
            private const val HELL_THREAD = "ignore_events_with_too_many_mentions"
            private const val DMS_FROM_FOLLOWS = "only_show_dm_notifications_from_users_i_follow"
            private const val REACTIONS_FROM_FOLLOWS = "only_show_reactions_from_users_i_follow"
            private const val NOTIFICATION_AMOUNT_THRESHOLD = "show_wallet_push_notifications_above_sats"

            fun valueOf(id: String): Preferences? {
                return when (id) {
                    HELL_THREAD -> HellThread
                    DMS_FROM_FOLLOWS -> DMsFromFollows
                    REACTIONS_FROM_FOLLOWS -> ReactionsFromFollows
                    NOTIFICATION_AMOUNT_THRESHOLD -> NotificationAmountThreshold
                    else -> null
                }
            }
        }
    }

    sealed class TabNotifications(
        id: String,
        order: Int,
        val types: List<NotificationType>,
    ) : NotificationSettingsType(id = id, order = order) {

        data object NewFollows : TabNotifications(
            id = "NEW_FOLLOWS",
            types = listOf(
                NEW_USER_FOLLOWED_YOU,
            ),
            order = 1,
        )

        data object Zaps : TabNotifications(
            id = "ZAPS",
            types = listOf(
                YOUR_POST_WAS_ZAPPED,
                POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED,
                POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED,
            ),
            order = 2,
        )

        data object Reactions : TabNotifications(
            id = "REACTIONS",
            types = listOf(
                YOUR_POST_WAS_LIKED,
                YOUR_POST_WAS_BOOKMARKED,
                YOUR_POST_WAS_HIGHLIGHTED,
                POST_YOU_WERE_MENTIONED_IN_WAS_LIKED,
                POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED,
            ),
            order = 3,
        )

        data object Replies : TabNotifications(
            id = "REPLIES",
            types = listOf(
                YOUR_POST_WAS_REPLIED_TO,
                POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO,
                POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO,
            ),
            order = 4,
        )

        data object Reposts : TabNotifications(
            id = "REPOSTS",
            types = listOf(
                YOUR_POST_WAS_REPOSTED,
                POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED,
                POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED,
            ),
            order = 5,
        )

        data object Mentions : TabNotifications(
            id = "MENTIONS",
            types = listOf(
                YOU_WERE_MENTIONED_IN_POST,
                YOUR_POST_WAS_MENTIONED_IN_POST,
            ),
            order = 6,
        )
    }
}
