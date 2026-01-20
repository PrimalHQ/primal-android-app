package net.primal.android.settings.developer

import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import net.primal.android.R
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.core.logging.AppLogExporter
import net.primal.android.settings.developer.DeveloperToolsContract.SideEffect
import net.primal.android.settings.developer.DeveloperToolsContract.UiEvent
import net.primal.android.settings.developer.DeveloperToolsContract.UiState
import net.primal.android.theme.AppTheme

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

                Spacer(modifier = Modifier.height(24.dp))

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
            }
        },
    )
}
