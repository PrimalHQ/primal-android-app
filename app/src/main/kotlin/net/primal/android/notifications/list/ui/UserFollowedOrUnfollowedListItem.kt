package net.primal.android.notifications.list.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.notifications.db.Notification
import net.primal.android.notifications.db.NotificationData
import net.primal.android.notifications.domain.NotificationType
import net.primal.android.profile.db.authorNameUiFriendly
import net.primal.android.theme.PrimalTheme

@Composable
private fun FollowerListItem(
    notifications: List<Notification>,
    imagePainter: Painter,
    suffixText: String,
    onProfileClick: (String) -> Unit,
) {
    NotificationListItem(
        iconPainter = imagePainter,
    ) {
        val firstNotification = notifications.first()
        val firstFollower = firstNotification.owner

        Column {
            AvatarThumbnailsRow(
                avatarUrls = notifications.map { it.owner?.picture },
                onClick = {
                    firstFollower?.ownerId?.let(onProfileClick)
                },
            )

            val andOthersText = pluralStringResource(
                R.plurals.notification_list_item_and_others,
                notifications.size - 1,
                notifications.size - 1,
            )
            NostrUserText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                displayName = firstFollower?.authorNameUiFriendly()
                    ?: firstNotification.data.ownerId.asEllipsizedNpub(),
                internetIdentifier = firstFollower?.internetIdentifier,
                annotatedStringSuffixBuilder = {
                    if (notifications.size > 1) append(" $andOthersText")
                    append(" $suffixText")
                }
            )
        }
    }
}

@Composable
fun UserFollowedYouListItem(
    notifications: List<Notification>,
    onProfileClick: (String) -> Unit,
) {
    FollowerListItem(
        notifications = notifications,
        imagePainter = painterResource(
            id = if (isSystemInDarkTheme()) {
                R.drawable.notification_type_new_user_followed_you_dark
            } else {
                R.drawable.notification_type_new_user_followed_you_light
            }
        ),
        suffixText = stringResource(id = R.string.notification_list_item_followed_you),
        onProfileClick = onProfileClick,
    )
}

@Composable
fun UserUnfollowedYouListItem(
    notifications: List<Notification>,
    onProfileClick: (String) -> Unit,
) {
    FollowerListItem(
        notifications = notifications,
        imagePainter = painterResource(
            id = if (isSystemInDarkTheme()) {
                R.drawable.notification_type_user_unfollowed_you_dark
            } else {
                R.drawable.notification_type_user_unfollowed_you_light
            }
        ),
        suffixText = stringResource(id = R.string.notification_list_item_unfollowed_you),
        onProfileClick = onProfileClick,
    )
}

@Preview
@Composable
fun PreviewNewUserFollowedYourListItem() {
    PrimalTheme {
        Surface {
            UserFollowedYouListItem(
                notifications = listOf(
                    Notification(
                        data = NotificationData(
                            ownerId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                            createdAt = 0L,
                            type = NotificationType.NEW_USER_FOLLOWED_YOU,
                            actionByUserId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                        )
                    ),
                    Notification(
                        data = NotificationData(
                            ownerId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                            createdAt = 0L,
                            type = NotificationType.NEW_USER_FOLLOWED_YOU,
                            actionByUserId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                        )
                    ),
                ),
                onProfileClick = {},
            )
        }
    }
}
