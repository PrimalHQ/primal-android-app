package net.primal.android.settings.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.settings.content.ContentDisplaySettingsContract.UiEvent
import net.primal.android.user.domain.ContentDisplaySettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentDisplaySettingsScreen(viewModel: ContentDisplaySettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.uiState.collectAsState()

    ContentDisplaySettingsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@Composable
@ExperimentalMaterial3Api
private fun ContentDisplaySettingsScreen(
    state: ContentDisplaySettingsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_content_display_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_content_display_auto_play_videos),
                    supportText = stringResource(id = R.string.settings_content_display_auto_play_videos_hint),
                    trailingContent = {
                        PrimalSwitch(
                            checked = state.autoPlayVideos == ContentDisplaySettings.AUTO_PLAY_VIDEO_ALWAYS,
                            onCheckedChange = {
                                eventPublisher(
                                    UiEvent.UpdateAutoPlayVideos(
                                        code = if (it) {
                                            ContentDisplaySettings.AUTO_PLAY_VIDEO_ALWAYS
                                        } else {
                                            ContentDisplaySettings.AUTO_PLAY_VIDEO_NEVER
                                        },
                                    ),
                                )
                            },
                        )
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_content_display_animated_avatars),
                    supportText = stringResource(id = R.string.settings_content_display_animated_avatars_hint),
                    trailingContent = {
                        PrimalSwitch(
                            checked = state.showAnimatedAvatars,
                            onCheckedChange = {
                                eventPublisher(UiEvent.UpdateShowAnimatedAvatars(enabled = it))
                            },
                        )
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_content_display_full_screen_feed_display),
                    supportText = stringResource(id = R.string.settings_content_display_full_screen_feed_display_hint),
                    trailingContent = {
                        PrimalSwitch(
                            checked = state.focusMode,
                            onCheckedChange = {
                                eventPublisher(UiEvent.UpdateShowFocusMode(enabled = it))
                            },
                        )
                    },
                )
            }
        },
    )
}
