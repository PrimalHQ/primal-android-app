package net.primal.android.settings.notifications

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.res.painterResource
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun NotificationsSettingsScreen(
    viewModel: NotificationsSettingsViewModel,
    onClose: () -> Unit
) {
    val state = viewModel.state.collectAsState()

    NotificationsSettingsScreen(
        state = state.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    state: NotificationsSettingsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (NotificationsSettingsContract.UiEvent) -> Unit
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = "Notifications",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(top = 56.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                state.notifications.entries.forEach {
                    NotificationsSettingsBlock(
                        title = it.key,
                        values = it.value,
                        eventPublisher = eventPublisher
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    )
}

@Composable
fun NotificationsSettingsBlock(
    title: String,
    values: List<NotificationsSettingsContract.Notification>,
    eventPublisher: (NotificationsSettingsContract.UiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .background(
                    AppTheme.extraColorScheme.surfaceVariantAlt,
                    shape = RoundedCornerShape(12.dp)
                )
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(size = 12.dp)
                )
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            values.forEach { value ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(
                            lightResId = value.lightResId,
                            darkResId = value.darkResId
                        ),
                        contentDescription = null
                    )
                    Text(
                        text = value.name,
                        fontWeight = FontWeight.W400,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .padding(bottom = 5.dp) // yuck
                    )
                    Switch(checked = value.value, onCheckedChange = {
                        eventPublisher(
                            NotificationsSettingsContract.UiEvent.NotificationSettingsChanged(
                                id = value.id,
                                value = it
                            )
                        )
                    })
                }
                if (values.indexOf(value) < values.lastIndex)
                    Divider(color = AppTheme.colorScheme.outline, thickness = 1.dp)
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewNotificationsSettingsScreen() {
    PrimalTheme {
        NotificationsSettingsScreen(
            state = NotificationsSettingsContract.UiState(
                mapOf(
                    "CORE NOTIFICATIONS" to listOf(
                        NotificationsSettingsContract.Notification(
                            id = "NEW_USER_FOLLOWED_YOU",
                            name = "new user followed you",
                            lightResId = R.drawable.notification_type_new_user_followed_you_light,
                            darkResId = R.drawable.notification_type_new_user_followed_you_dark,
                            value = true
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "USER_UNFOLLOWED_YOU",
                            name = "user unfollowed you",
                            lightResId = R.drawable.notification_type_user_unfollowed_you_light,
                            darkResId = R.drawable.notification_type_user_unfollowed_you_dark,
                            value = false
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "YOUR_POST_WAS_ZAPPED",
                            name = "your post was zapped",
                            lightResId = R.drawable.notification_type_your_post_was_zapped_light,
                            darkResId = R.drawable.notification_type_your_post_was_zapped_dark,
                            value = true
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "YOUR_POST_WAS_LIKED",
                            name = "your post was liked",
                            lightResId = R.drawable.notification_type_your_post_was_liked_light,
                            darkResId = R.drawable.notification_type_your_post_was_liked_dark,
                            value = true
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "YOUR_POST_WAS_REPOSTED",
                            name = "your post was reposted",
                            lightResId = R.drawable.notification_type_your_post_was_reposted_light,
                            darkResId = R.drawable.notification_type_your_post_was_reposted_dark,
                            value = true
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "YOUR_POST_WAS_REPLIED_TO",
                            name = "your post was replied to",
                            lightResId = R.drawable.notification_type_your_post_was_replied_to_light,
                            darkResId = R.drawable.notification_type_your_post_was_replied_to_dark,
                            value = true
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "YOU_WERE_MENTIONED_IN_POST",
                            name = "you were mentioned",
                            lightResId = R.drawable.notification_type_new_user_followed_you_light, // TODO: update when new icons become available
                            darkResId = R.drawable.notification_type_new_user_followed_you_dark, // TODO: update when new icons become available
                            value = true
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "YOUR_POST_WAS_MENTIONED_IN_POST",
                            name = "your post was mentioned",
                            lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_light, // TODO: update when new icons become available
                            darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_dark, // TODO: update when new icons become available
                            value = true
                        )
                    ),
                    "A NOTE YOU WERE MENTIONED IN WAS" to listOf(
                        NotificationsSettingsContract.Notification(
                            id = "POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED",
                            name = "zapped",
                            lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_light,
                            darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_dark,
                            value = true
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "POST_YOU_WERE_MENTIONED_IN_WAS_LIKED",
                            name = "liked",
                            lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_light,
                            darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_dark,
                            value = true
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED",
                            name = "reposted",
                            lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_light,
                            darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_dark,
                            value = true
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO",
                            name = "replied to",
                            lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_light,
                            darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_dark,
                            value = true
                        )
                    ),
                    "A NOTE YOUR NOTE WAS MENTIONED IN WAS" to listOf(
                        NotificationsSettingsContract.Notification(
                            id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED",
                            name = "zapped",
                            lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_light,
                            darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_dark,
                            value = false
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED",
                            name = "liked",
                            lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_light,
                            darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_dark,
                            value = false
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED",
                            name = "reposted",
                            lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_light,
                            darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_dark,
                            value = false
                        ),
                        NotificationsSettingsContract.Notification(
                            id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO",
                            name = "replied to",
                            lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_light,
                            darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_dark,
                            value = false
                        )
                    )
                )
            ),
            onClose = {},
            eventPublisher = {}
        )
    }
}