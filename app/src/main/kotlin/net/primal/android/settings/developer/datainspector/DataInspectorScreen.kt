package net.primal.android.settings.developer.datainspector

import android.content.Intent
import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import net.primal.android.R
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Share
import net.primal.android.settings.developer.datainspector.DataInspectorContract.SideEffect
import net.primal.android.settings.developer.datainspector.DataInspectorContract.UiEvent
import net.primal.android.settings.developer.datainspector.DataInspectorContract.UiState
import net.primal.android.theme.AppTheme
import net.primal.core.utils.onFailure
import net.primal.core.utils.runCatching

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataInspectorScreen(viewModel: DataInspectorViewModel, onClose: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SideEffect.ShareFile -> {
                    runCatching {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            effect.file,
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_STREAM, uri)
                            type = "application/octet-stream"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                context.getString(R.string.settings_developer_tools_data_inspector_export_chooser),
                            ),
                        )
                    }.onFailure {
                        Toast.makeText(
                            context,
                            context.getString(R.string.settings_developer_tools_data_inspector_export_failed),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
                is SideEffect.ExportFailed -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_developer_tools_data_inspector_export_failed),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    DataInspectorScreen(
        state = uiState,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataInspectorScreen(
    state: UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    val context = LocalContext.current

    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_developer_tools_data_inspector_screen_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .background(color = AppTheme.colorScheme.surfaceVariant)
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    item(key = "summary") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.settings_developer_tools_data_inspector_intro,
                                ),
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(
                                    id = R.string.settings_developer_tools_data_inspector_summary,
                                    state.files.size,
                                    Formatter.formatShortFileSize(context, state.totalSizeBytes),
                                ),
                                style = AppTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                    val grouped = state.files.groupBy { it.topLevelFolder }
                    grouped.forEach { (folder, files) ->
                        val groupSizeBytes = files.sumOf { it.sizeBytes }
                        item(key = "header_$folder") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = AppTheme.extraColorScheme.surfaceVariantAlt1)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = folder.ifBlank {
                                        stringResource(id = R.string.settings_developer_tools_data_inspector_root)
                                    },
                                    style = AppTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colorScheme.onPrimary,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = stringResource(
                                        id = R.string.settings_developer_tools_data_inspector_group_stats,
                                        files.size,
                                        Formatter.formatShortFileSize(context, groupSizeBytes),
                                    ),
                                    style = AppTheme.typography.bodySmall,
                                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                                )
                            }
                        }
                        items(
                            count = files.size,
                            key = { index -> files[index].absolutePath },
                        ) { index ->
                            val file = files[index]
                            DataFileRow(
                                name = file.relativePath.removePrefix("$folder/"),
                                sizeBytes = file.sizeBytes,
                                onExport = { eventPublisher(UiEvent.ExportFile(file.absolutePath)) },
                            )
                        }
                    }
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DataFileRow(name: String, sizeBytes: Long, onExport: () -> Unit) {
    val context = LocalContext.current
    ListItem(
        headlineContent = {
            Text(
                text = name,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
            )
        },
        supportingContent = {
            Text(
                text = Formatter.formatShortFileSize(context, sizeBytes),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        },
        trailingContent = {
            IconButton(onClick = onExport) {
                Icon(
                    imageVector = PrimalIcons.Share,
                    contentDescription = stringResource(
                        id = R.string.settings_developer_tools_data_inspector_export,
                    ),
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
    )
}
