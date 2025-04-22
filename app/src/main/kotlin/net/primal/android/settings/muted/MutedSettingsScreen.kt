package net.primal.android.settings.muted

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.settings.muted.tabs.HASHTAGS_INDEX
import net.primal.android.settings.muted.tabs.MUTE_SETTINGS_TAB_COUNT
import net.primal.android.settings.muted.tabs.MuteHashtags
import net.primal.android.settings.muted.tabs.MuteThreads
import net.primal.android.settings.muted.tabs.MuteUsers
import net.primal.android.settings.muted.tabs.MuteWords
import net.primal.android.settings.muted.tabs.MutedSettingsTabs
import net.primal.android.settings.muted.tabs.THREADS_INDEX
import net.primal.android.settings.muted.tabs.USERS_INDEX
import net.primal.android.settings.muted.tabs.WORDS_INDEX
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.links.CdnImage

@Composable
fun MutedSettingsScreen(
    viewModel: MutedSettingsViewModel,
    noteCallbacks: NoteCallbacks,
    onProfileClick: (String) -> Unit,
    onClose: () -> Unit,
    onGoToWallet: () -> Unit,
) {
    val state = viewModel.state.collectAsState()
    MutedSettingsScreen(
        state = state.value,
        eventPublisher = viewModel::setEvent,
        onProfileClick = onProfileClick,
        onClose = onClose,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MutedSettingsScreen(
    state: MutedSettingsContract.UiState,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit,
    onProfileClick: (String) -> Unit,
    onClose: () -> Unit,
) {
    val pagerState = rememberPagerState { MUTE_SETTINGS_TAB_COUNT }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        state.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = it.resolveUiErrorMessage(context),
                    duration = SnackbarDuration.Short,
                )
            }
            eventPublisher(MutedSettingsContract.UiEvent.DismissError())
        }
    }

    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_muted_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
                footer = {
                    MutedSettingsTabs(
                        modifier = Modifier,
                        selectedTabIndex = pagerState.currentPage,
                        onUsersTabClick = { scope.launch { pagerState.animateScrollToPage(USERS_INDEX) } },
                        onWordsTabClick = { scope.launch { pagerState.animateScrollToPage(WORDS_INDEX) } },
                        onHashtagsTabClick = { scope.launch { pagerState.animateScrollToPage(HASHTAGS_INDEX) } },
                        onThreadsTabClick = { scope.launch { pagerState.animateScrollToPage(THREADS_INDEX) } },
                    )
                },
            )
        },
        content = { paddingValues ->
            MutedSettingsPager(
                pagerState = pagerState,
                state = state,
                paddingValues = paddingValues,
                noteCallbacks = noteCallbacks,
                onGoToWallet = onGoToWallet,
                eventPublisher = eventPublisher,
                onProfileClick = onProfileClick,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    )
}

@Composable
private fun MutedSettingsPager(
    pagerState: PagerState,
    state: MutedSettingsContract.UiState,
    paddingValues: PaddingValues,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    HorizontalPager(state = pagerState) { pageIndex ->
        when (pageIndex) {
            USERS_INDEX -> MuteUsers(
                modifier = Modifier.background(AppTheme.colorScheme.surfaceVariant),
                paddingValues = paddingValues,
                mutedUsers = state.mutedUsers,
                eventPublisher = eventPublisher,
                onProfileClick = onProfileClick,
            )
            WORDS_INDEX -> MuteWords(
                modifier = Modifier.background(AppTheme.colorScheme.surfaceVariant),
                paddingValues = paddingValues,
                mutedWords = state.mutedWords,
                newMutedWord = state.newMutedWord,
                eventPublisher = eventPublisher,
            )
            HASHTAGS_INDEX -> MuteHashtags(
                modifier = Modifier.background(AppTheme.colorScheme.surfaceVariant),
                paddingValues = paddingValues,
                mutedHashtags = state.mutedHashtags,
                newMutedHashtag = state.newMutedHashtag,
                eventPublisher = eventPublisher,
            )
            THREADS_INDEX -> MuteThreads(
                defaultMuteThreadsFeedSpec = state.defaultMuteThreadsFeedSpec,
                paddingValues = paddingValues,
                noteCallbacks = noteCallbacks,
                onGoToWallet = onGoToWallet,
            )
        }
    }
}

@Preview
@Composable
fun PreviewMutedScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        MutedSettingsScreen(
            state = MutedSettingsContract.UiState(
                mutedUsers = listOf(
                    ProfileDetailsUi(
                        pubkey = "pubkey",
                        authorDisplayName = "username",
                        userDisplayName = "username",
                        avatarCdnImage = null,
                        internetIdentifier = "nip05",
                    ),
                    ProfileDetailsUi(
                        pubkey = "pubkey",
                        authorDisplayName = "username",
                        userDisplayName = "username",
                        avatarCdnImage = CdnImage("avatarUrl"),
                        internetIdentifier = "nip05",
                    ),
                    ProfileDetailsUi(
                        pubkey = "pubkey",
                        authorDisplayName = "username",
                        userDisplayName = "username",
                        avatarCdnImage = null,
                        internetIdentifier = "nip05",
                    ),
                    ProfileDetailsUi(
                        pubkey = "pubkey",
                        authorDisplayName = "username",
                        userDisplayName = "username",
                        avatarCdnImage = null,
                        internetIdentifier = "nip05",
                    ),
                    ProfileDetailsUi(
                        pubkey = "pubkey",
                        authorDisplayName = "username",
                        userDisplayName = "username",
                        avatarCdnImage = CdnImage("avatarUrl"),
                        internetIdentifier = "nip05",
                    ),
                ),
            ),
            eventPublisher = {},
            onProfileClick = {},
            onClose = {},
            onGoToWallet = {},
            noteCallbacks = NoteCallbacks(),
        )
    }
}
