package net.primal.android.settings.media

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.DeleteListItemImage
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.ConnectRelay
import net.primal.android.core.compose.settings.DecoratedSettingsOutlinedTextField
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.settings.network.ConfirmActionAlertDialog
import net.primal.android.settings.network.TextSection
import net.primal.android.theme.AppTheme

@Composable
fun MediaUploadsSettingsScreen(viewModel: MediaUploadsSettingsViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()
    MediaUploadsSettingsScreen(
        state = state.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaUploadsSettingsScreen(
    state: MediaUploadsSettingsContract.UiState,
    eventPublisher: (MediaUploadsSettingsContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val backSequence = {
        if (state.mode == MediaUploadsMode.View) {
            onClose()
        } else {
            focusManager.clearFocus()
            eventPublisher(
                MediaUploadsSettingsContract.UiEvent.UpdateMediaUploadsMode(mode = MediaUploadsMode.View),
            )
        }
    }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = {
            eventPublisher(MediaUploadsSettingsContract.UiEvent.DismissError())
        },
    )

    BackHandler {
        backSequence()
    }

    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_media_uploads_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = backSequence,
            )
        },
        content = { paddingValues ->
            if (state.isLoadingBlossomServerUrls) {
                PrimalLoadingSpinner()
            } else {
                MediaUploadsLazyColumn(
                    modifier = Modifier
                        .background(color = AppTheme.colorScheme.surfaceVariant)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(paddingValues)
                        .imePadding(),
                    state = state,
                    focusManager = focusManager,
                    eventPublisher = eventPublisher,
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    )
}

@Suppress("ComplexMethod")
@Composable
private fun MediaUploadsLazyColumn(
    modifier: Modifier,
    state: MediaUploadsSettingsContract.UiState,
    focusManager: FocusManager,
    eventPublisher: (MediaUploadsSettingsContract.UiEvent) -> Unit,
) {
    var confirmingRestoreDefaultBlossomServerDialog by remember { mutableStateOf(false) }
    if (confirmingRestoreDefaultBlossomServerDialog) {
        ConfirmActionAlertDialog(
            dialogTitle = stringResource(id = R.string.settings_media_uploads_restore_default_blossom_server_title),
            dialogText = stringResource(
                id = R.string.settings_media_uploads_restore_default_blossom_server_description,
            ),
            onDismissRequest = {
                confirmingRestoreDefaultBlossomServerDialog = false
            },
            onConfirmation = {
                confirmingRestoreDefaultBlossomServerDialog = false
                eventPublisher(MediaUploadsSettingsContract.UiEvent.RestoreDefaultBlossomServer)
            },
        )
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val isEditMainBlossom = state.mode == MediaUploadsMode.EditBlossomServer
    val isEditMirrorBlossom = state.mode == MediaUploadsMode.EditBlossomMirrorServer
    val isViewMode = state.mode == MediaUploadsMode.View

    LazyColumn(modifier = modifier) {
        if (isViewMode) {
            blossomMainServerSectionItem(state.blossomServerUrl)
        }

        if (isEditMainBlossom || isViewMode) {
            blossomMainServerInputItem(
                state = state,
                eventPublisher = eventPublisher,
                keyboardController = keyboardController,
                onRestoreDefaultBlossomServer = { confirmingRestoreDefaultBlossomServerDialog = true },
            )
            item { PrimalDivider() }
        }

        if (isViewMode) {
            blossomMirrorServerSectionItem(
                blossomMirrorEnabled = state.blossomMirrorEnabled,
                onBlossomMirrorCheckedChange = {
                    eventPublisher(
                        MediaUploadsSettingsContract.UiEvent.UpdateBlossomMirrorEnabled(it),
                    )
                },
            )
        }

        if (state.blossomMirrorEnabled) {
            if (isViewMode && state.mirrorBlossomServerUrls.isNotEmpty()) {
                item {
                    MirrorBlossomServerSection(
                        mirrorBlossomServerUrls = state.mirrorBlossomServerUrls,
                        onDisconnectMirrorBlossomServer = {
                            eventPublisher(MediaUploadsSettingsContract.UiEvent.RemoveBlossomMirrorServerUrl(it))
                        },
                    )
                }
            }

            if (isEditMirrorBlossom || isViewMode) {
                blossomMirrorServerInputItem(state, eventPublisher, keyboardController)
            }

            if (isEditMirrorBlossom) {
                item { PrimalDivider() }
            }
        }

        if (isEditMainBlossom || isEditMirrorBlossom) {
            suggestedBlossomServersSectionItems(
                suggestedBlossomServerUrls = state.suggestedBlossomServers,
                confirmBlossomServerUrl = { url ->
                    val event = if (isEditMainBlossom) {
                        MediaUploadsSettingsContract.UiEvent.ConfirmBlossomServerUrl(url)
                    } else {
                        MediaUploadsSettingsContract.UiEvent.ConfirmBlossomMirrorServerUrl(url)
                    }
                    focusManager.clearFocus()
                    eventPublisher(event)
                },
            )
        }
    }
}

private fun LazyListScope.suggestedBlossomServersSectionItems(
    suggestedBlossomServerUrls: List<String>,
    confirmBlossomServerUrl: (String) -> Unit,
) {
    item {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.settings_media_uploads_suggested_blossoms_title),
                style = AppTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    items(suggestedBlossomServerUrls) { server ->
        SuggestedServerItem(
            server = server,
            onClick = {
                confirmBlossomServerUrl(server)
            },
        )
    }
}

@Composable
private fun SuggestedServerItem(server: String, onClick: () -> Unit) {
    val success = AppTheme.extraColorScheme.successBright

    ListItem(
        modifier = Modifier.clickable { onClick() },
        leadingContent = {
            Box(
                modifier = Modifier
                    .padding(start = 2.dp, top = 2.dp)
                    .size(10.dp)
                    .drawWithCache {
                        onDrawWithContent {
                            drawCircle(color = success)
                        }
                    },
            )
        },
        headlineContent = {
            Text(text = server, color = AppTheme.extraColorScheme.onSurfaceVariantAlt1)
        },
        trailingContent = {
            AppBarIcon(
                modifier = Modifier.padding(bottom = 4.dp, start = 8.dp),
                icon = PrimalIcons.ConnectRelay,
                tint = AppTheme.colorScheme.primary,
                iconSize = 19.dp,
                onClick = onClick,
                appBarIconContentDescription = "",
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
    )
}

private fun LazyListScope.blossomMainServerSectionItem(blossomServerUrl: String) {
    item {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            TextSection(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.settings_media_uploads_blossom_server_section_title).uppercase(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            BlossomServerDestination(destinationUrl = blossomServerUrl)
        }
    }
}

private fun LazyListScope.blossomMainServerInputItem(
    state: MediaUploadsSettingsContract.UiState,
    eventPublisher: (MediaUploadsSettingsContract.UiEvent) -> Unit,
    keyboardController: SoftwareKeyboardController?,
    onRestoreDefaultBlossomServer: () -> Unit,
) {
    item(key = "blossom_main_input") {
        DecoratedSettingsOutlinedTextField(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            title = stringResource(id = R.string.settings_media_uploads_switch_blossom_server),
            placeholderText = stringResource(id = R.string.settings_media_uploads_switch_blossom_server_placeholder),
            value = state.newBlossomServerUrl,
            onValueChanged = {
                eventPublisher(MediaUploadsSettingsContract.UiEvent.UpdateNewBlossomServerUrl(url = it))
            },
            onFocus = {
                eventPublisher(
                    MediaUploadsSettingsContract.UiEvent.UpdateMediaUploadsMode(
                        mode = MediaUploadsMode.EditBlossomServer,
                    ),
                )
            },
            supportingActionText = stringResource(R.string.settings_media_uploads_restore_default_blossom_server),
            onSupportActionClick = {
                keyboardController?.hide()
                onRestoreDefaultBlossomServer()
            },
            showSupportContent = state.mode == MediaUploadsMode.View,
            buttonEnabled = isBlossomServerConfirmEnabled(
                state.blossomServerUrl,
                state.newBlossomServerUrl,
                state.mirrorBlossomServerUrls,
            ),
            onActionClick = {
                keyboardController?.hide()
                eventPublisher(
                    MediaUploadsSettingsContract.UiEvent.ConfirmBlossomServerUrl(url = state.newBlossomServerUrl),
                )
            },
        )
    }

    if (state.mode == MediaUploadsMode.EditBlossomServer) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun LazyListScope.blossomMirrorServerInputItem(
    state: MediaUploadsSettingsContract.UiState,
    eventPublisher: (MediaUploadsSettingsContract.UiEvent) -> Unit,
    keyboardController: SoftwareKeyboardController?,
) {
    item(key = "blossom_mirror_input") {
        DecoratedSettingsOutlinedTextField(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            title = stringResource(id = R.string.settings_media_uploads_switch_blossom_mirror_server),
            placeholderText = stringResource(id = R.string.settings_media_uploads_switch_blossom_server_placeholder),
            value = state.newBlossomServerMirrorUrl,
            onValueChanged = {
                eventPublisher(MediaUploadsSettingsContract.UiEvent.UpdateNewBlossomMirrorServerUrl(url = it))
            },
            onFocus = {
                eventPublisher(
                    MediaUploadsSettingsContract.UiEvent.UpdateMediaUploadsMode(
                        mode = MediaUploadsMode.EditBlossomMirrorServer,
                    ),
                )
            },
            buttonEnabled = isBlossomServerConfirmEnabled(
                state.blossomServerUrl,
                state.newBlossomServerMirrorUrl,
                state.mirrorBlossomServerUrls,
            ),
            onActionClick = {
                keyboardController?.hide()
                eventPublisher(
                    MediaUploadsSettingsContract.UiEvent.ConfirmBlossomMirrorServerUrl(
                        url = state.newBlossomServerMirrorUrl,
                    ),
                )
            },
        )
    }

    if (state.mode == MediaUploadsMode.EditBlossomMirrorServer) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun LazyListScope.blossomMirrorServerSectionItem(
    blossomMirrorEnabled: Boolean,
    onBlossomMirrorCheckedChange: (Boolean) -> Unit,
) {
    item {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            TextSection(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(
                    id = R.string.settings_media_uploads_blossom_mirror_server_section_title,
                ).uppercase(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            ListItem(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(AppTheme.shapes.medium),
                headlineContent = {
                    Text(
                        text = stringResource(
                            id = R.string.settings_media_uploads_blossom_mirror_server_switcher_title,
                        ),
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colorScheme.onPrimary,
                    )
                },
                trailingContent = {
                    PrimalSwitch(
                        checked = blossomMirrorEnabled,
                        onCheckedChange = onBlossomMirrorCheckedChange,
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                ),
            )
            Text(
                modifier = Modifier.padding(horizontal = 17.dp).padding(top = 10.dp, bottom = 15.dp),
                text = stringResource(id = R.string.settings_media_uploads_blossom_mirror_server_enabled_notice),
                style = AppTheme.typography.bodySmall,
                lineHeight = 20.sp,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }
    }
}

@Composable
private fun BlossomServerDestination(
    modifier: Modifier = Modifier,
    destinationUrl: String,
    connected: Boolean = true,
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
    ),
) {
    val success = AppTheme.extraColorScheme.successBright
    val failed = AppTheme.colorScheme.error

    ListItem(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(AppTheme.shapes.medium),
        leadingContent = {
            Box(
                modifier = Modifier
                    .padding(start = 2.dp, top = 2.dp)
                    .size(10.dp)
                    .drawWithCache {
                        this.onDrawWithContent {
                            drawCircle(color = if (connected) success else failed)
                        }
                    },
            )
        },
        headlineContent = {
            Text(text = destinationUrl.removeHttpPrefix())
        },
        colors = colors,
    )
}

@Composable
private fun MirrorBlossomServerSection(
    modifier: Modifier = Modifier,
    mirrorBlossomServerUrls: List<String>,
    onDisconnectMirrorBlossomServer: (String) -> Unit,
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
    ),
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        mirrorBlossomServerUrls.forEachIndexed { index, server ->
            val isFirst = index == 0
            val isLast = index == mirrorBlossomServerUrls.lastIndex

            val shape = when {
                isFirst && isLast -> AppTheme.shapes.medium
                isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                else -> RoundedCornerShape(0.dp)
            }

            MirrorBlossomServerDestination(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape),
                destinationUrl = server,
                onDisconnectMirrorBlossomServer = {
                    onDisconnectMirrorBlossomServer(server)
                },
                colors = colors,
            )

            if (!isLast) {
                PrimalDivider()
            }
        }
    }
}

@Composable
private fun MirrorBlossomServerDestination(
    modifier: Modifier = Modifier,
    destinationUrl: String,
    onDisconnectMirrorBlossomServer: () -> Unit,
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
    ),
) {
    val gray = AppTheme.colorScheme.outline

    ListItem(
        modifier = modifier,
        leadingContent = {
            Box(
                modifier = Modifier
                    .padding(start = 2.dp, top = 2.dp)
                    .size(10.dp)
                    .drawWithCache {
                        this.onDrawWithContent {
                            drawCircle(color = gray)
                        }
                    },
            )
        },
        headlineContent = {
            Text(text = destinationUrl.removeHttpPrefix())
        },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    modifier = Modifier.offset(x = 7.dp),
                    onClick = onDisconnectMirrorBlossomServer,
                ) {
                    DeleteListItemImage()
                }
            }
        },
        colors = colors,
    )
}

private fun String.removeHttpPrefix() = this.removePrefix("https://").removePrefix("http://")

private fun isBlossomServerConfirmEnabled(
    currentUrl: String,
    newUrl: String,
    mirrorUrls: List<String>,
): Boolean =
    newUrl.isNotEmpty() &&
        newUrl != currentUrl &&
        newUrl !in mirrorUrls
