package net.primal.android.settings.notifications

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    LaunchedErrorHandler(viewModel = viewModel)

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
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val grouped = state.notifications.groupBy { it.group }
                grouped.forEach {
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
                        modifier = Modifier
                            .width(28.dp)
                            .height(20.dp), // normalize the size since we're mixing svgs and pngs with different sizes
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(id = value.textResId),
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

@Composable
fun LaunchedErrorHandler(
    viewModel: NotificationsSettingsViewModel
) {
    val genericMessage = stringResource(id = R.string.app_generic_error)
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.state.filter { it.error != null }.map { it.error }.filterNotNull().collect {
            uiScope.launch {
                Toast.makeText(
                    context, genericMessage, Toast.LENGTH_SHORT
                ).show()
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
                notifications = listOf(
                    NotificationsSettingsContract.Notification(
                        id = "NEW_USER_FOLLOWED_YOU",
                        textResId = R.string.settings_notifications_new_user_followed_you_text,
                        lightResId = R.drawable.notification_type_new_user_followed_you_light,
                        darkResId = R.drawable.notification_type_new_user_followed_you_dark,
                        group = "CORE NOTIFICATIONS",
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOUR_POST_WAS_ZAPPED",
                        textResId = R.string.settings_notifications_your_post_was_zapped_text,
                        lightResId = R.drawable.notification_type_your_post_was_zapped_light,
                        darkResId = R.drawable.notification_type_your_post_was_zapped_dark,
                        group = "CORE NOTIFICATIONS",
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOUR_POST_WAS_LIKED",
                        textResId = R.string.settings_notifications_your_post_was_liked_text,
                        lightResId = R.drawable.notification_type_your_post_was_liked_light,
                        darkResId = R.drawable.notification_type_your_post_was_liked_dark,
                        group = "CORE NOTIFICATIONS",
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOUR_POST_WAS_REPOSTED",
                        textResId = R.string.settings_notifications_your_post_was_reposted_text,
                        lightResId = R.drawable.notification_type_your_post_was_reposted_light,
                        darkResId = R.drawable.notification_type_your_post_was_reposted_dark,
                        group = "CORE NOTIFICATIONS",
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOUR_POST_WAS_REPLIED_TO",
                        textResId = R.string.settings_notifications_your_post_was_replied_to_text,
                        lightResId = R.drawable.notification_type_your_post_was_replied_to_light,
                        darkResId = R.drawable.notification_type_your_post_was_replied_to_dark,
                        group = "CORE NOTIFICATIONS",
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOU_WERE_MENTIONED_IN_POST",
                        textResId = R.string.settings_notifications_you_were_mentioned_text,
                        lightResId = R.drawable.notification_type_you_were_mentioned_in_a_post_light,
                        darkResId = R.drawable.notification_type_you_were_mentioned_in_a_post_dark,
                        group = "CORE NOTIFICATIONS",
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "YOUR_POST_WAS_MENTIONED_IN_POST",
                        textResId = R.string.settings_notifications_your_post_was_mentioned_text,
                        lightResId = R.drawable.notification_type_your_post_was_mentioned_in_a_post_light,
                        darkResId = R.drawable.notification_type_your_post_was_mentioned_in_a_post_dark,
                        group = "CORE NOTIFICATIONS",
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED",
                        textResId = R.string.settings_notifications_post_you_were_mentioned_in_was_zapped_text,
                        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_light,
                        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_zapped_dark,
                        group = "A NOTE YOU WERE MENTIONED IN WAS",
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOU_WERE_MENTIONED_IN_WAS_LIKED",
                        textResId = R.string.settings_notifications_post_you_were_mentioned_in_was_liked_text,
                        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_light,
                        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_liked_dark,
                        group = "A NOTE YOU WERE MENTIONED IN WAS",
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED",
                        textResId = R.string.settings_notifications_post_you_were_mentioned_in_was_reposted_text,
                        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_light,
                        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_reposted_dark,
                        group = "A NOTE YOU WERE MENTIONED IN WAS",
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO",
                        textResId = R.string.settings_notifications_post_you_were_mentioned_in_was_replied_to_text,
                        lightResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_light,
                        darkResId = R.drawable.notification_type_post_you_were_mentioned_in_was_replied_to_dark,
                        group = "A NOTE YOU WERE MENTIONED IN WAS",
                        value = true
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED",
                        textResId = R.string.settings_notifications_post_your_post_was_mentioned_in_was_zapped_text,
                        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_light,
                        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_zapped_dark,
                        group = "A NOTE YOUR NOTE WAS MENTIONED IN WAS",
                        value = false
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED",
                        textResId = R.string.settings_notifications_post_your_post_was_mentioned_in_was_liked_text,
                        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_light,
                        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_liked_dark,
                        group = "A NOTE YOUR NOTE WAS MENTIONED IN WAS",
                        value = false
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED",
                        textResId = R.string.settings_notifications_post_your_post_was_mentioned_in_was_reposted_text,
                        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_light,
                        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_reposted_dark,
                        group = "A NOTE YOUR NOTE WAS MENTIONED IN WAS",
                        value = false
                    ),
                    NotificationsSettingsContract.Notification(
                        id = "POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO",
                        textResId = R.string.settings_notifications_post_your_post_was_mentioned_in_was_replied_to_text,
                        lightResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_light,
                        darkResId = R.drawable.notification_type_post_your_post_was_mentioned_in_was_replied_to_dark,
                        group = "A NOTE YOUR NOTE WAS MENTIONED IN WAS",
                        value = false
                    )
                )
            ),
            onClose = {},
            eventPublisher = {}
        )
    }
}