package net.primal.android.settings.notifications

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.notifications.toImagePainter
import net.primal.android.notifications.domain.NotificationSection
import net.primal.android.notifications.domain.NotificationType
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiState.ApiError
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun NotificationsSettingsScreen(viewModel: NotificationsSettingsViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    LaunchedErrorHandler(viewModel = viewModel)

    NotificationsSettingsScreen(
        state = state.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    state: NotificationsSettingsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (NotificationsSettingsContract.UiEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_notifications_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                state.notificationSwitches
                    .groupBy { it.notificationType.section }
                    .forEach { (section, notifications) ->
                        item {
                            NotificationsSettingsBlock(
                                section = section,
                                notifications = notifications,
                                eventPublisher = eventPublisher,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        },
    )
}

@Composable
fun NotificationsSettingsBlock(
    section: NotificationSection,
    notifications: List<NotificationSwitchUi>,
    eventPublisher: (NotificationsSettingsContract.UiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            text = section.toTitle().uppercase(),
            fontWeight = FontWeight.Medium,
            style = AppTheme.typography.bodySmall,
        )
        Column(
            modifier = Modifier
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt3,
                    shape = RoundedCornerShape(12.dp),
                )
                .fillMaxWidth()
                .clip(RoundedCornerShape(size = 12.dp)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            notifications.forEachIndexed { index, notificationSwitchUi ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier.size(32.dp),
                        painter = notificationSwitchUi.notificationType.toImagePainter(),
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .padding(bottom = 5.dp),
                        text = notificationSwitchUi.notificationType.toTitle(),
                        fontWeight = FontWeight.W400,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    PrimalSwitch(
                        checked = notificationSwitchUi.enabled,
                        onCheckedChange = {
                            eventPublisher(
                                NotificationsSettingsContract.UiEvent.NotificationSettingChanged(
                                    type = notificationSwitchUi.notificationType,
                                    value = it,
                                ),
                            )
                        },
                    )
                }

                if (index < notifications.size - 1) {
                    PrimalDivider()
                }
            }
        }
    }
}

@Composable
private fun NotificationSection.toTitle(): String =
    when (this) {
        NotificationSection.CORE_NOTIFICATIONS ->
            stringResource(id = R.string.settings_notifications_section_core)

        NotificationSection.NOTE_YOU_WERE_MENTIONED_IN ->
            stringResource(id = R.string.settings_notifications_section_note_you_were_mentioned_in)

        NotificationSection.NOTE_YOUR_NOTE_WAS_MENTIONED_IN ->
            stringResource(
                id = R.string.settings_notifications_section_note_your_note_was_mentioned_in,
            )
    }

@Composable
private fun NotificationType.toTitle(): String =
    when (this) {
        NotificationType.NEW_USER_FOLLOWED_YOU ->
            stringResource(id = R.string.settings_notifications_new_user_followed_you_text)

        NotificationType.YOUR_POST_WAS_ZAPPED ->
            stringResource(id = R.string.settings_notifications_your_post_was_zapped_text)

        NotificationType.YOUR_POST_WAS_LIKED ->
            stringResource(id = R.string.settings_notifications_your_post_was_liked_text)

        NotificationType.YOUR_POST_WAS_REPOSTED ->
            stringResource(id = R.string.settings_notifications_your_post_was_reposted_text)

        NotificationType.YOUR_POST_WAS_REPLIED_TO ->
            stringResource(id = R.string.settings_notifications_your_post_was_replied_to_text)

        NotificationType.YOU_WERE_MENTIONED_IN_POST ->
            stringResource(id = R.string.settings_notifications_you_were_mentioned_text)

        NotificationType.YOUR_POST_WAS_MENTIONED_IN_POST ->
            stringResource(id = R.string.settings_notifications_your_post_was_mentioned_text)

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_ZAPPED ->
            stringResource(
                id = R.string.settings_notifications_post_you_were_mentioned_in_was_zapped_text,
            )

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_LIKED ->
            stringResource(
                id = R.string.settings_notifications_post_you_were_mentioned_in_was_liked_text,
            )

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPOSTED ->
            stringResource(
                id = R.string.settings_notifications_post_you_were_mentioned_in_was_reposted_text,
            )

        NotificationType.POST_YOU_WERE_MENTIONED_IN_WAS_REPLIED_TO ->
            stringResource(
                id = R.string.settings_notifications_post_you_were_mentioned_in_was_replied_to_text,
            )

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_ZAPPED ->
            stringResource(
                id = R.string.settings_notifications_post_your_post_was_mentioned_in_was_zapped_text,
            )

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_LIKED ->
            stringResource(
                id = R.string.settings_notifications_post_your_post_was_mentioned_in_was_liked_text,
            )

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPOSTED ->
            stringResource(
                id = R.string.settings_notifications_post_your_post_was_mentioned_in_was_reposted_text,
            )

        NotificationType.POST_YOUR_POST_WAS_MENTIONED_IN_WAS_REPLIED_TO ->
            stringResource(
                id = R.string.settings_notifications_post_your_post_was_mentioned_in_was_replied_to_text,
            )
    }

@Composable
fun LaunchedErrorHandler(viewModel: NotificationsSettingsViewModel) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.state
            .mapNotNull { it.error }
            .map {
                context.getString(
                    when (it) {
                        is ApiError.FetchAppSettingsError -> R.string.settings_notifications_error_fetch_settings
                        is ApiError.UpdateAppSettingsError -> R.string.settings_notifications_error_update_settings
                    },
                )
            }
            .collect {
                viewModel.setEvent(NotificationsSettingsContract.UiEvent.DismissErrors)
                uiScope.launch {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }
    }
}

@Preview
@Composable
fun PreviewNotificationsSettingsScreen() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        NotificationsSettingsScreen(
            state = NotificationsSettingsContract.UiState(),
            onClose = {},
            eventPublisher = {},
        )
    }
}
