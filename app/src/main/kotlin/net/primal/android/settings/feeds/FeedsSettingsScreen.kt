package net.primal.android.settings.feeds

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun FeedsSettingsScreen(
    viewModel: FeedsSettingsViewModel,
    onClose: () -> Unit
) {
    val uiState = viewModel.state.collectAsState()

    FeedsSettingsScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsSettingsScreen(
    state: FeedsSettingsContract.UiState,
    eventPublisher: (FeedsSettingsContract.UiEvent) -> Unit,
    onClose: () -> Unit
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_feeds_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose
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
                            eventPublisher(FeedsSettingsContract.UiEvent.FeedRemoved(directive = prompt.directive))

                            openRemoveFeedDialog.value = FeedAction.Inactive
                        },
                        dialogTitle = stringResource(id = R.string.settings_feeds_remove_feed_prompt_title),
                        dialogText = stringResource(id = R.string.settings_feeds_remove_feed_prompt_text, prompt.name),
                        icon = Icons.Default.Warning
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
                        dialogText = stringResource(id = R.string.settings_feeds_restore_defaults_prompt_text),
                        icon = Icons.Default.Warning
                    )
                }

                is FeedAction.Inactive -> Unit
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = stringResource(R.string.settings_feeds_list_title).uppercase(),
                        fontWeight = FontWeight.W500,
                        fontSize = 14.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                                    openDialog = true
                                )
                            }
                        )
                        Divider(color = AppTheme.colorScheme.outline, thickness = 1.dp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1.0f))
                    Text(
                        modifier = Modifier.clickable {
                            openRemoveFeedDialog.value = FeedAction.ConfirmRestoreDefaults
                        },
                        text = stringResource(R.string.settings_feeds_restore_default_title).lowercase(),
                        fontWeight = FontWeight.W500,
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                        color = AppTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}

@Composable
fun FeedItem(
    item: Feed,
    onRemoveFeed: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.isRemovable) {
            IconButton(onClick = onRemoveFeed) {
                Image(
                    imageVector = Icons.Filled.RemoveCircle,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = Color.Red)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(width = 48.dp))
        }
        Text(
            text = item.name,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.weight(1.0f))
    }
}

@Composable
fun ConfirmActionAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = null)
        },
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
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
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
                        isRemovable = false
                    ),
                    Feed(
                        name = "Latest with Replies",
                        directive = "Latest with Replies",
                        isRemovable = false
                    ),
                    Feed(
                        name = "Trending 24h",
                        directive = "Trending 24h",
                        isRemovable = true
                    ),
                    Feed(
                        name = "Most zapped 4h",
                        directive = "Most zapped 4h",
                        isRemovable = true
                    ),
                    Feed(
                        name = "#photography",
                        directive = "#photography",
                        isRemovable = true
                    ),
                    Feed(
                        name = "#bitcoin2023",
                        directive = "#bitcoin2023",
                        isRemovable = true
                    ),
                    Feed(
                        name = "#nostrasia",
                        directive = "#nostrasia",
                        isRemovable = true
                    ),
                    Feed(
                        name = "#nature",
                        directive = "#nature",
                        isRemovable = true
                    ),
                    Feed(
                        name = "#food",
                        directive = "#food",
                        isRemovable = true
                    )
                )
            ),
            eventPublisher = {},
            onClose = {})
    }
}