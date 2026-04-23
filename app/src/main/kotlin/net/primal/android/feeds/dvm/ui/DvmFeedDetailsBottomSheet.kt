package net.primal.android.feeds.dvm.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.errors.UiError
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DvmFeedDetailsBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    dvmFeed: DvmFeedUi,
    addedToFeed: Boolean,
    addToUserFeeds: (DvmFeedUi) -> Unit,
    removeFromUserFeeds: (DvmFeedUi) -> Unit,
    noteCallbacks: NoteCallbacks = NoteCallbacks(),
    onGoToWallet: (() -> Unit)? = null,
    onUiError: ((UiError) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val handleCloseBottomSheet: () -> Unit = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismissRequest()
            }
        }
    }

    BackHandler {
        handleCloseBottomSheet()
    }

    ModalBottomSheet(
        modifier = modifier.statusBarsPadding(),
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
    ) {
        PrimalScaffold(
            topBar = {
                Column {
                    TopAppBar(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .padding(vertical = 16.dp),
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = handleCloseBottomSheet) {
                                Icon(
                                    imageVector = PrimalIcons.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        },
                        actions = {
                            ActionButton(
                                addedToFeed = addedToFeed,
                                addToUserFeeds = { addToUserFeeds(dvmFeed) },
                                removeFromUserFeeds = { removeFromUserFeeds(dvmFeed) },
                            )
                        },
                    )
                    PrimalDivider()
                }
            },
        ) { paddingValues ->
            DvmHeaderAndFeedList(
                modifier = Modifier.padding(paddingValues),
                dvmFeed = dvmFeed,
                noteCallbacks = noteCallbacks,
                onGoToWallet = onGoToWallet,
                extended = true,
                showFollowsActionsAvatarRow = true,
                clipShape = null,
                onUiError = onUiError,
            )
        }
    }
}

@Composable
private fun ActionButton(
    addedToFeed: Boolean,
    addToUserFeeds: () -> Unit,
    removeFromUserFeeds: () -> Unit,
) {
    if (addedToFeed) {
        TextButton(
            onClick = removeFromUserFeeds,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                contentColor = AppTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(id = R.string.explore_feeds_dvm_details_action_button_remove),
                fontWeight = FontWeight.W600,
            )
        }
    } else {
        TextButton(
            onClick = addToUserFeeds,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = AppTheme.colorScheme.onPrimary,
                contentColor = AppTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Text(
                text = stringResource(id = R.string.explore_feeds_dvm_details_action_button_add),
                fontWeight = FontWeight.W600,
            )
        }
    }
}
