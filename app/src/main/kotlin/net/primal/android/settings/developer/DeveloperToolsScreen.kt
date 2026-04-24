package net.primal.android.settings.developer

import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.ConfirmActionAlertDialog
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.dropdown.DropdownPrimalMenuItem
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Copy
import net.primal.android.core.compose.icons.primaliconpack.Delete
import net.primal.android.core.compose.icons.primaliconpack.Key
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.core.logging.AppLogExporter
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.settings.developer.DeveloperToolsContract.DevWalletInfo
import net.primal.android.settings.developer.DeveloperToolsContract.SideEffect
import net.primal.android.settings.developer.DeveloperToolsContract.UiEvent
import net.primal.android.settings.developer.DeveloperToolsContract.UiState
import net.primal.android.theme.AppTheme
import net.primal.domain.wallet.WalletType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperToolsScreen(viewModel: DeveloperToolsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SideEffect.ShareLogs -> {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        effect.file,
                    )
                    AppLogExporter.shareLogs(context, uri)
                }
                is SideEffect.NoLogsToExport -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_developer_tools_error_no_logs),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                is SideEffect.ExportFailed -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_developer_tools_error_export_failed),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                is SideEffect.SeedWordsCopied -> {
                    context.copyText(text = effect.seedWords)
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_developer_tools_seed_words_copied),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                is SideEffect.SeedWordsCopyFailed -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_developer_tools_seed_words_copy_failed),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                is SideEffect.WalletDeleted -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_developer_tools_wallet_deleted),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                is SideEffect.WalletDeleteFailed -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_developer_tools_wallet_delete_failed),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    DeveloperToolsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@Suppress("LongMethod")
@Composable
@ExperimentalMaterial3Api
private fun DeveloperToolsScreen(
    state: UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    val context = LocalContext.current

    PrimalScaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_developer_tools_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Logging section
                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_developer_tools_logging_title),
                    supportText = stringResource(id = R.string.settings_developer_tools_logging_description),
                    trailingContent = {
                        PrimalSwitch(
                            checked = state.isLoggingEnabled,
                            onCheckedChange = {
                                eventPublisher(UiEvent.ToggleLogging(enabled = it))
                            },
                        )
                    },
                    onClick = {
                        eventPublisher(UiEvent.ToggleLogging(enabled = !state.isLoggingEnabled))
                    },
                )

                if (state.logFileCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(
                            id = R.string.settings_developer_tools_log_stats,
                            state.logFileCount,
                            Formatter.formatShortFileSize(context, state.totalLogSizeBytes),
                        ),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                PrimalFilledButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    onClick = { eventPublisher(UiEvent.ExportLogs) },
                    enabled = state.logFileCount > 0 && !state.isExporting,
                ) {
                    Text(
                        text = if (state.isExporting) {
                            stringResource(id = R.string.settings_developer_tools_exporting)
                        } else {
                            stringResource(id = R.string.settings_developer_tools_export)
                        },
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PrimalFilledButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    onClick = { eventPublisher(UiEvent.ClearLogs) },
                    enabled = state.logFileCount > 0,
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    contentColor = AppTheme.colorScheme.onSurface,
                ) {
                    Text(text = stringResource(id = R.string.settings_developer_tools_clear))
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Wallet section
                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_developer_tools_wallet_picker_title),
                    supportText = stringResource(id = R.string.settings_developer_tools_wallet_picker_description),
                    trailingContent = {
                        PrimalSwitch(
                            checked = state.isWalletPickerEnabled,
                            onCheckedChange = {
                                eventPublisher(UiEvent.ToggleWalletPicker(enabled = it))
                            },
                        )
                    },
                    onClick = {
                        eventPublisher(UiEvent.ToggleWalletPicker(enabled = !state.isWalletPickerEnabled))
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                state.wallets.forEach { wallet ->
                    WalletItem(
                        wallet = wallet,
                        onCopyWalletId = {
                            context.copyText(text = wallet.walletId)
                            Toast.makeText(
                                context,
                                context.getString(R.string.settings_developer_tools_wallet_id_copied),
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        onCopySeedWords = {
                            eventPublisher(UiEvent.CopySeedWords(walletId = wallet.walletId))
                        },
                        onDeleteWallet = {
                            eventPublisher(UiEvent.DeleteWallet(walletId = wallet.walletId))
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        },
    )
}

@Suppress("LongMethod")
@Composable
private fun WalletItem(
    wallet: DevWalletInfo,
    onCopyWalletId: () -> Unit,
    onCopySeedWords: () -> Unit,
    onDeleteWallet: () -> Unit,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    var menuVisible by rememberSaveable { mutableStateOf(false) }
    var deleteDialogVisible by rememberSaveable { mutableStateOf(false) }

    if (deleteDialogVisible) {
        ConfirmActionAlertDialog(
            dialogTitle = stringResource(id = R.string.settings_developer_tools_delete_wallet_title),
            dialogText = stringResource(id = R.string.settings_developer_tools_delete_wallet_text),
            confirmText = stringResource(id = R.string.context_confirm_delete_positive),
            dismissText = stringResource(id = R.string.context_confirm_delete_negative),
            onConfirmation = {
                deleteDialogVisible = false
                onDeleteWallet()
            },
            onDismissRequest = { deleteDialogVisible = false },
        )
    }

    val supportText = buildString {
        if (!wallet.lightningAddress.isNullOrBlank()) {
            append(wallet.lightningAddress)
            append(" · ")
        }
        append(wallet.walletId.ellipsizeMiddle(size = 8))
    }

    ListItem(
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                Text(
                    text = wallet.type.toDisplayName(),
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colorScheme.onPrimary,
                )
                if (wallet.balanceInSats != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${numberFormat.format(wallet.balanceInSats)} ${
                            stringResource(id = R.string.wallet_sats_suffix)
                        }",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    )
                }
                if (wallet.isActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACTIVE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(bottom = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colorScheme.primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .padding(top = 3.dp),
                    )
                }
            }
        },
        supportingContent = {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = supportText,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        },
        trailingContent = {
            Box {
                IconButton(onClick = { menuVisible = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                    )
                }

                DropdownPrimalMenu(
                    expanded = menuVisible,
                    onDismissRequest = { menuVisible = false },
                ) {
                    DropdownPrimalMenuItem(
                        trailingIconVector = PrimalIcons.Copy,
                        iconSize = 20.dp,
                        text = stringResource(id = R.string.settings_developer_tools_wallet_id_copy),
                        onClick = {
                            menuVisible = false
                            onCopyWalletId()
                        },
                    )
                    if (wallet.type == WalletType.SPARK) {
                        DropdownPrimalMenuItem(
                            trailingIconVector = PrimalIcons.Key,
                            iconSize = 20.dp,
                            text = stringResource(id = R.string.settings_developer_tools_seed_words_copy),
                            onClick = {
                                menuVisible = false
                                onCopySeedWords()
                            },
                        )
                        DropdownPrimalMenuItem(
                            trailingIconVector = PrimalIcons.Delete,
                            iconSize = 20.dp,
                            tint = AppTheme.colorScheme.error,
                            text = stringResource(id = R.string.settings_developer_tools_delete_wallet),
                            onClick = {
                                menuVisible = false
                                deleteDialogVisible = true
                            },
                        )
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
    )
}

private fun WalletType.toDisplayName(): String =
    when (this) {
        WalletType.PRIMAL -> "Primal Wallet"
        WalletType.SPARK -> "Spark Wallet"
        WalletType.NWC -> "NWC Wallet"
    }
