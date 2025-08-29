package net.primal.android.core.compose.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import net.primal.android.R
import net.primal.android.core.compose.res.painterResource
import net.primal.domain.notifications.NotificationType

@Composable
fun NotificationType.toImagePainter(): Painter =
    when (this) {
        NotificationType.NEW_USER_FOLLOWED_YOU -> painterResource(
            darkResId = R.drawable.notification_type_followed_dark,
            lightResId = R.drawable.notification_type_followed_light,
        )

        NotificationType.YOUR_POST_WAS_ZAPPED -> painterResource(
            darkResId = R.drawable.notification_type_your_post_was_zapped_dark,
            lightResId = R.drawable.notification_type_your_post_was_zapped_light,
        )

        NotificationType.YOUR_POST_WAS_LIKED -> painterResource(
            darkResId = R.drawable.notification_type_your_post_was_liked_dark,
            lightResId = R.drawable.notification_type_your_post_was_liked_light,
        )

        NotificationType.YOUR_POST_WAS_REPOSTED -> painterResource(
            darkResId = R.drawable.notification_type_your_post_was_reposted_dark,
            lightResId = R.drawable.notification_type_your_post_was_reposted_light,
        )

        NotificationType.YOUR_POST_WAS_REPLIED_TO -> painterResource(
            darkResId = R.drawable.notification_type_your_post_was_replied_to_dark,
            lightResId = R.drawable.notification_type_your_post_was_replied_to_light,
        )

        NotificationType.YOU_WERE_MENTIONED_IN_POST -> painterResource(
            darkResId = R.drawable.notification_type_you_were_mentioned_in_a_post_dark,
            lightResId = R.drawable.notification_type_you_were_mentioned_in_a_post_light,
        )

        NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST -> painterResource(
            darkResId = R.drawable.notification_type_your_post_was_mentioned_in_a_post_dark,
            lightResId = R.drawable.notification_type_your_post_was_mentioned_in_a_post_light,
        )

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED -> painterResource(
            darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_dark,
            lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_light,
        )

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED -> painterResource(
            darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_dark,
            lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_light,
        )

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED -> painterResource(
            darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_dark,
            lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_light,
        )

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO -> painterResource(
            darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_dark,
            lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_light,
        )

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED -> painterResource(
            darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_dark,
            lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_light,
        )

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED -> painterResource(
            darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_dark,
            lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_light,
        )

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED -> painterResource(
            darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_dark,
            lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_light,
        )

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO -> painterResource(
            darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_dark,
            lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_light,
        )

        NotificationType.YOUR_POST_WAS_HIGHLIGHTED -> painterResource(
            darkResId = R.drawable.notification_type_highlighted_dark,
            lightResId = R.drawable.notification_type_highlighted_light,
        )

        NotificationType.YOUR_POST_WAS_BOOKMARKED -> painterResource(
            darkResId = R.drawable.notification_type_bookmarked_dark,
            lightResId = R.drawable.notification_type_bookmarked_light,
        )

        NotificationType.LIVE_EVENT_HAPPENING -> painterResource(
            darkResId = R.drawable.notification_type_live_stream_dark,
            lightResId = R.drawable.notification_type_live_stream_light,
        )
    }
