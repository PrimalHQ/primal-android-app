package net.primal.android.premium.manage.content

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.ContextBroadcast
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.premium.manage.content.model.ContentGroup
import net.primal.android.premium.ui.ManagePremiumTableRow
import net.primal.android.theme.AppTheme

@Composable
fun PremiumContentBackupScreen(viewModel: PremiumContentBackupViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(
                PremiumContentBackupContract.UiEvent.StartBroadcastingMonitor,
            )

            Lifecycle.Event.ON_STOP -> viewModel.setEvent(PremiumContentBackupContract.UiEvent.StopBroadcastingMonitor)
            else -> Unit
        }
    }

    PremiumContentBackupScreen(
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
    )
}

private const val CountWeight = 0.25f
private const val TypeWeight = 0.40f
private const val RebroadcastWeight = 0.35f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumContentBackupScreen(
    state: PremiumContentBackupContract.UiState,
    eventPublisher: (PremiumContentBackupContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_content_backup_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .padding(top = 16.dp)
                .fillMaxSize()
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                        shape = AppTheme.shapes.large.copy(
                            bottomEnd = CornerSize(0.dp),
                            bottomStart = CornerSize(0.dp),
                        ),
                    ),
            ) {
                ManagePremiumTableRow(
                    firstColumn = stringResource(R.string.premium_content_backup_table_count),
                    firstColumnWeight = CountWeight,
                    secondColumn = stringResource(R.string.premium_content_backup_table_type),
                    secondColumnWeight = TypeWeight,
                    thirdColumn = stringResource(R.string.premium_content_backup_table_rebroadcast),
                    thirdColumnWeight = RebroadcastWeight,
                    thirdColumnTextAlign = TextAlign.End,
                    fontWeight = FontWeight.SemiBold,
                )
                PrimalDivider()
            }

            state.contentTypes.forEachIndexed { index, item ->
                val isLastItem = index == state.contentTypes.size - 1

                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .background(
                            color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                            shape = if (isLastItem) {
                                AppTheme.shapes.large.copy(
                                    topEnd = CornerSize(0.dp),
                                    topStart = CornerSize(0.dp),
                                )
                            } else {
                                RectangleShape
                            },
                        ),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    ContentListItem(
                        count = item.count,
                        title = item.group.toHumanString(),
                        broadcasting = item.broadcasting,
                        progress = item.progress,
                        enabled = !state.anyBroadcasting,
                        onBroadcastClick = {
                            eventPublisher(PremiumContentBackupContract.UiEvent.StartBroadcasting(type = item))
                        },
                    )

                    if (!isLastItem) {
                        PrimalDivider()
                    }
                }
            }

            if (state.anyBroadcasting) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { eventPublisher(PremiumContentBackupContract.UiEvent.StopBroadcasting) }) {
                    Text(
                        text = stringResource(R.string.premium_content_backup_stop_rebroadcast),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
private fun ContentListItem(
    count: Long?,
    title: String,
    broadcasting: Boolean,
    progress: Float,
    enabled: Boolean,
    onBroadcastClick: () -> Unit,
) {
    val numberFormat = NumberFormat.getNumberInstance()

    ManagePremiumTableRow(
        modifier = Modifier.fillMaxHeight(),
        firstColumnWeight = CountWeight,
        firstColumn = {
            if (count != null) {
                Text(
                    text = numberFormat.format(count),
                    style = AppTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    color = AppTheme.colorScheme.onSurface,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                )
            }
        },
        secondColumnWeight = TypeWeight,
        secondColumn = {
            Text(
                text = title,
                style = AppTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = AppTheme.colorScheme.onSurface,
            )
        },
        thirdColumnWeight = RebroadcastWeight,
        thirdColumnContentAlignment = Alignment.CenterEnd,
        thirdColumn = {
            if (broadcasting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.padding(end = 8.dp, top = 2.dp),
                        text = "${(progress * 100).toInt().coerceIn(0..100)}%",
                        style = AppTheme.typography.bodySmall,
                        fontSize = 12.sp,
                    )

                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                Icon(
                    modifier = Modifier.clickable(enabled = enabled, onClick = onBroadcastClick),
                    imageVector = PrimalIcons.ContextBroadcast,
                    contentDescription = "Rebroadcast $title",
                    tint = if (enabled) AppTheme.colorScheme.secondary else AppTheme.colorScheme.outline,
                )
            }
        },
    )
}

@Composable
private fun ContentGroup.toHumanString(): String =
    when (this) {
        ContentGroup.Notes -> stringResource(R.string.premium_content_backup_table_type_notes)
        ContentGroup.Reactions -> stringResource(R.string.premium_content_backup_table_type_reactions)
        ContentGroup.DMs -> stringResource(R.string.premium_content_backup_table_type_dms)
        ContentGroup.Articles -> stringResource(R.string.premium_content_backup_table_type_articles)
        ContentGroup.All -> stringResource(R.string.premium_content_backup_all_events)
    }
