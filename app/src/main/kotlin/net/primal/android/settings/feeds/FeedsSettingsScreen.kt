package net.primal.android.settings.feeds

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.settings.feeds.model.Feed
import net.primal.android.settings.feeds.model.FeedAction
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun FeedsSettingsScreen(viewModel: FeedsSettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    FeedsSettingsScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsSettingsScreen(
    state: FeedsSettingsContract.UiState,
    eventPublisher: (FeedsSettingsContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_feeds_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            val openRemoveFeedDialog =
                remember { mutableStateOf<FeedAction>(FeedAction.Inactive) }
            when (val prompt = openRemoveFeedDialog.value) {
                is FeedAction.ConfirmRemove -> {
                    ConfirmActionAlertDialog(
                        onDismissRequest = {
                            openRemoveFeedDialog.value = FeedAction.Inactive
                        },
                        onConfirmation = {
                            eventPublisher(
                                FeedsSettingsContract.UiEvent.FeedRemoved(
                                    directive = prompt.directive,
                                ),
                            )

                            openRemoveFeedDialog.value = FeedAction.Inactive
                        },
                        dialogTitle = stringResource(
                            id = R.string.settings_feeds_remove_feed_prompt_title,
                        ),
                        dialogText = stringResource(
                            id = R.string.settings_feeds_remove_feed_prompt_text,
                            prompt.name,
                        ),
                    )
                }

                is FeedAction.ConfirmRestoreDefaults -> {
                    ConfirmActionAlertDialog(
                        onDismissRequest = {
                            openRemoveFeedDialog.value = FeedAction.Inactive
                        },
                        onConfirmation = {
                            eventPublisher(FeedsSettingsContract.UiEvent.RestoreDefaultFeeds)

                            openRemoveFeedDialog.value = FeedAction.Inactive
                        },
                        dialogTitle = stringResource(R.string.settings_feeds_restore_default_title),
                        dialogText = stringResource(
                            id = R.string.settings_feeds_restore_defaults_prompt_text,
                        ),
                    )
                }

                is FeedAction.Inactive -> Unit
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(state.feeds) { item ->
                        FeedItem(
                            item = item,
                            onRemoveFeed = {
                                openRemoveFeedDialog.value = FeedAction.ConfirmRemove(
                                    directive = item.directive,
                                    name = item.name,
                                    openDialog = true,
                                )
                            },
                        )
                        PrimalDivider()
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Text(
                                modifier = Modifier.clickable {
                                    openRemoveFeedDialog.value = FeedAction.ConfirmRestoreDefaults
                                },
                                text = stringResource(
                                    R.string.settings_feeds_restore_default_title,
                                ).lowercase(),
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
fun FeedItem(item: Feed, onRemoveFeed: () -> Unit) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        headlineContent = {
            Text(
                text = item.name,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                lineHeight = 16.sp,
            )
        },

        leadingContent = {
            Box(
                modifier = Modifier
                    .clickable(
                        enabled = item.isRemovable,
                        onClick = onRemoveFeed,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Spacer(
                    modifier = Modifier
                        .size(16.dp)
                        .background(color = Color.White, shape = CircleShape),
                )

                Image(
                    imageVector = Icons.Outlined.RemoveCircle,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        color = if (item.isRemovable) {
                            AppTheme.colorScheme.error
                        } else {
                            AppTheme.colorScheme.outline
                        },
                    ),
                )
            }
        },
    )
}

@Composable
fun ConfirmActionAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                },
            ) {
                Text(
                    text = stringResource(id = R.string.settings_feeds_dialog_confirm),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
            ) {
                Text(
                    text = stringResource(id = R.string.settings_feeds_dialog_dismiss),
                )
            }
        },
    )
}

@Preview
@Composable
fun PreviewSettingsScreen() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        FeedsSettingsScreen(
            state = FeedsSettingsContract.UiState(
                feeds = listOf(
                    Feed(
                        name = "Latest",
                        directive = "Latest",
                        isRemovable = false,
                    ),
                    Feed(
                        name = "Latest with Replies",
                        directive = "Latest with Replies",
                        isRemovable = false,
                    ),
                    Feed(
                        name = "Trending 24h",
                        directive = "Trending 24h",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "Most zapped 4h",
                        directive = "Most zapped 4h",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "#photography",
                        directive = "#photography",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "#bitcoin2023",
                        directive = "#bitcoin2023",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "#nostrasia",
                        directive = "#nostrasia",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "#nature",
                        directive = "#nature",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "#food",
                        directive = "#food",
                        isRemovable = true,
                    ),
                ),
            ),
            eventPublisher = {},
            onClose = {},
        )
    }
}
