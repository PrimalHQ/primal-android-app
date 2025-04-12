package net.primal.android.settings.notifications

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.painter.Painter
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
import net.primal.android.core.compose.SignatureErrorColumn
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.res.painterResource
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiEvent.NotificationSettingsChanged
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiState.ApiError
import net.primal.android.settings.notifications.ui.NotificationSwitchUi
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.notifications.NotificationSettingsSection
import net.primal.domain.notifications.NotificationSettingsType

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
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_notifications_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            SignatureErrorColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues,
                signatureUiError = state.signatureError,
            ) {
                NotificationsColumn(
                    modifier = Modifier.padding(paddingValues),
                    state = state,
                    eventPublisher = eventPublisher,
                )
            }
        },
    )
}

@Composable
private fun NotificationsColumn(
    modifier: Modifier = Modifier,
    state: NotificationsSettingsContract.UiState,
    eventPublisher: (NotificationsSettingsContract.UiEvent) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .background(color = AppTheme.colorScheme.surfaceVariant)
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            // TODO Implement push notifications render
        }

        if (state.enabledPushNotifications || true) {
            item {
                NotificationsSettingsBlock(
                    section = NotificationSettingsSection.PUSH_NOTIFICATIONS,
                    notifications = state.pushNotificationsSettings,
                    eventPublisher = eventPublisher,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            NotificationsSettingsBlock(
                section = NotificationSettingsSection.NOTIFICATIONS_IN_TAB,
                notifications = state.tabNotificationsSettings,
                eventPublisher = eventPublisher,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            NotificationsSettingsBlock(
                section = NotificationSettingsSection.PREFERENCES,
                notifications = state.preferencesSettings,
                eventPublisher = eventPublisher,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun <T : NotificationSettingsType> NotificationsSettingsBlock(
    section: NotificationSettingsSection,
    notifications: List<NotificationSwitchUi<T>>,
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
                val isLargerRow = notificationSwitchUi.settingsType is NotificationSettingsType.Preferences
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            eventPublisher(
                                NotificationSettingsChanged(
                                    type = notificationSwitchUi.settingsType,
                                    value = !notificationSwitchUi.enabled,
                                ),
                            )
                        }
                        .padding(horizontal = 16.dp)
                        .padding(vertical = if (isLargerRow) 8.dp else 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val painter = notificationSwitchUi.settingsType.toImagePainter()
                    if (painter != null) {
                        Image(
                            modifier = Modifier.size(32.dp),
                            painter = painter,
                            contentDescription = null,
                        )
                    }

                    Text(
                        modifier = Modifier
                            .fillMaxWidth(fraction = 0.75f)
                            .padding(top = 2.dp),
                        text = notificationSwitchUi.settingsType.toTitle(),
                        fontWeight = FontWeight.W400,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    PrimalSwitch(
                        checked = notificationSwitchUi.enabled,
                        onCheckedChange = {
                            eventPublisher(
                                NotificationSettingsChanged(
                                    type = notificationSwitchUi.settingsType,
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
@Deprecated("Replace with SnackbarErrorHandler")
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

@Composable
private fun NotificationSettingsSection.toTitle(): String =
    when (this) {
        NotificationSettingsSection.PUSH_NOTIFICATIONS -> stringResource(R.string.settings_notifications_section_push)
        NotificationSettingsSection.NOTIFICATIONS_IN_TAB -> stringResource(R.string.settings_notifications_section_tab)
        NotificationSettingsSection.PREFERENCES -> stringResource(R.string.settings_notifications_section_preferences)
    }

@Composable
private fun NotificationSettingsType.toTitle(): String =
    when (this) {
        NotificationSettingsType.Preferences.DMsFromFollows -> stringResource(
            R.string.settings_notifications_group_preferences_DMS_from_follows,
        )
        NotificationSettingsType.Preferences.HellThread -> stringResource(
            R.string.settings_notifications_group_preferences_hell_thread,
        )
        NotificationSettingsType.Preferences.ReactionsFromFollows -> stringResource(
            R.string.settings_notifications_group_preferences_reactions_from_follows,
        )
        NotificationSettingsType.PushNotifications.DirectMessages -> stringResource(
            R.string.settings_notifications_group_direct_messages,
        )
        NotificationSettingsType.PushNotifications.Mentions -> stringResource(
            R.string.settings_notifications_group_mentions,
        )
        NotificationSettingsType.PushNotifications.NewFollows -> stringResource(
            R.string.settings_notifications_group_new_followers,
        )
        NotificationSettingsType.PushNotifications.Reactions -> stringResource(
            R.string.settings_notifications_group_reactions,
        )
        NotificationSettingsType.PushNotifications.Replies -> stringResource(
            R.string.settings_notifications_group_replies,
        )
        NotificationSettingsType.PushNotifications.Reposts -> stringResource(
            R.string.settings_notifications_group_reposts,
        )
        NotificationSettingsType.PushNotifications.WalletTransactions -> stringResource(
            R.string.settings_notifications_group_wallet_txs,
        )
        NotificationSettingsType.PushNotifications.Zaps -> stringResource(R.string.settings_notifications_group_zaps)
        NotificationSettingsType.TabNotifications.Mentions -> stringResource(
            R.string.settings_notifications_group_mentions,
        )
        NotificationSettingsType.TabNotifications.NewFollows -> stringResource(
            R.string.settings_notifications_group_new_followers,
        )
        NotificationSettingsType.TabNotifications.Reactions -> stringResource(
            R.string.settings_notifications_group_reactions,
        )
        NotificationSettingsType.TabNotifications.Replies -> stringResource(
            R.string.settings_notifications_group_replies,
        )
        NotificationSettingsType.TabNotifications.Reposts -> stringResource(
            R.string.settings_notifications_group_reposts,
        )
        NotificationSettingsType.TabNotifications.Zaps -> stringResource(R.string.settings_notifications_group_zaps)
    }

@Composable
private fun NotificationSettingsType.toImagePainter(): Painter? =
    when (this) {
        is NotificationSettingsType.Preferences -> null
        NotificationSettingsType.PushNotifications.DirectMessages ->
            painterResource(
                darkResId = R.drawable.notification_push_type_dms_dark,
                lightResId = R.drawable.notification_push_type_dms_dark,
            )

        NotificationSettingsType.PushNotifications.Mentions ->
            painterResource(
                darkResId = R.drawable.notification_type_you_were_mentioned_in_a_post_dark,
                lightResId = R.drawable.notification_type_you_were_mentioned_in_a_post_light,
            )

        NotificationSettingsType.PushNotifications.NewFollows ->
            painterResource(
                darkResId = R.drawable.notification_type_followed_dark,
                lightResId = R.drawable.notification_type_followed_light,
            )

        NotificationSettingsType.PushNotifications.Reactions ->
            painterResource(
                darkResId = R.drawable.notification_type_your_post_was_liked_dark,
                lightResId = R.drawable.notification_type_your_post_was_liked_light,
            )

        NotificationSettingsType.PushNotifications.Replies ->
            painterResource(
                darkResId = R.drawable.notification_type_your_post_was_replied_to_dark,
                lightResId = R.drawable.notification_type_your_post_was_replied_to_light,
            )

        NotificationSettingsType.PushNotifications.Reposts ->
            painterResource(
                darkResId = R.drawable.notification_type_your_post_was_reposted_dark,
                lightResId = R.drawable.notification_type_your_post_was_reposted_light,
            )

        NotificationSettingsType.PushNotifications.WalletTransactions ->
            painterResource(
                darkResId = R.drawable.notification_push_type_wallet_tx_dark,
                lightResId = R.drawable.notification_push_type_wallet_tx_dark,
            )

        NotificationSettingsType.PushNotifications.Zaps ->
            painterResource(
                darkResId = R.drawable.notification_type_your_post_was_zapped_dark,
                lightResId = R.drawable.notification_type_your_post_was_zapped_light,
            )

        NotificationSettingsType.TabNotifications.Mentions ->
            painterResource(
                darkResId = R.drawable.notification_type_you_were_mentioned_in_a_post_dark,
                lightResId = R.drawable.notification_type_you_were_mentioned_in_a_post_light,
            )

        NotificationSettingsType.TabNotifications.NewFollows ->
            painterResource(
                darkResId = R.drawable.notification_type_followed_dark,
                lightResId = R.drawable.notification_type_followed_light,
            )

        NotificationSettingsType.TabNotifications.Reactions ->
            painterResource(
                darkResId = R.drawable.notification_type_your_post_was_liked_dark,
                lightResId = R.drawable.notification_type_your_post_was_liked_light,
            )

        NotificationSettingsType.TabNotifications.Replies ->
            painterResource(
                darkResId = R.drawable.notification_type_your_post_was_replied_to_dark,
                lightResId = R.drawable.notification_type_your_post_was_replied_to_light,
            )

        NotificationSettingsType.TabNotifications.Reposts ->
            painterResource(
                darkResId = R.drawable.notification_type_your_post_was_reposted_dark,
                lightResId = R.drawable.notification_type_your_post_was_reposted_light,
            )

        NotificationSettingsType.TabNotifications.Zaps ->
            painterResource(
                darkResId = R.drawable.notification_type_your_post_was_zapped_dark,
                lightResId = R.drawable.notification_type_your_post_was_zapped_light,
            )
    }

@Preview
@Composable
fun PreviewNotificationsSettingsScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        NotificationsSettingsScreen(
            state = NotificationsSettingsContract.UiState(),
            onClose = {},
            eventPublisher = {},
        )
    }
}
