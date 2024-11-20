package net.primal.android.premium.manage.contact

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.format.FormatStyle
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.premium.manage.contact.model.FollowListBackup
import net.primal.android.premium.ui.ManagePremiumTableRow
import net.primal.android.theme.AppTheme

@Composable
fun PremiumContactListScreen(viewModel: PremiumContactListViewModel, onClose: () -> Unit) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect {
            when (it) {
                PremiumContactListContract.SideEffect.RecoverFailed -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.premium_recover_contact_list_failed),
                        withDismissAction = true,
                    )
                }

                PremiumContactListContract.SideEffect.RecoverSuccessful -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.premium_recover_contact_list_success),
                        withDismissAction = true,
                    )
                }
            }
        }
    }

    PremiumContactListScreen(
        state = uiState,
        eventPublisher = viewModel::setEvent,
        snackbarHostState = snackbarHostState,
        onClose = onClose,
    )
}

private const val DateWeight = 0.4f
private const val FollowsWeight = 0.3f
private const val RecoverListWeight = 0.3f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun PremiumContactListScreen(
    state: PremiumContactListContract.UiState,
    eventPublisher: (PremiumContactListContract.UiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit,
) {
    var recoverFollowList by remember { mutableStateOf<FollowListBackup?>(null) }
    recoverFollowList?.let { backupItem ->
        RecoverFollowListDialog(
            backup = backupItem,
            onDismissRequest = { recoverFollowList = null },
            onConfirm = {
                recoverFollowList = null
                eventPublisher(PremiumContactListContract.UiEvent.RecoverFollowList(backup = backupItem))
            },
        )
    }

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_recover_contact_list_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .padding(top = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top),
        ) {
            stickyHeader {
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
                        firstColumn = stringResource(R.string.premium_recover_contact_list_table_date),
                        firstColumnWeight = DateWeight,
                        secondColumn = stringResource(R.string.premium_recover_contact_list_table_follows),
                        secondColumnWeight = FollowsWeight,
                        thirdColumn = stringResource(R.string.premium_recover_contact_list_table_recover_list),
                        thirdColumnWeight = RecoverListWeight,
                        thirdColumnTextAlign = TextAlign.End,
                        fontWeight = FontWeight.SemiBold,
                    )
                    PrimalDivider()
                }
            }

            itemsIndexed(state.backups) { index, item ->
                val isLastItem = index == state.backups.size - 1
                Column(
                    modifier = Modifier.background(
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
                ) {
                    ManagePremiumTableRow(
                        firstColumn = Instant.ofEpochSecond(item.timestamp).formatToDefaultDateFormat(
                            FormatStyle.MEDIUM,
                        ),
                        firstColumnWeight = DateWeight,
                        secondColumn = item.followsCount.toString(),
                        secondColumnWeight = FollowsWeight,
                        thirdColumn = stringResource(R.string.premium_recover_contact_list_table_recover_button),
                        thirdColumnWeight = RecoverListWeight,
                        thirdColumnTextColor = AppTheme.colorScheme.secondary,
                        thirdColumnTextAlign = TextAlign.End,
                        thirdColumnOnClick = { recoverFollowList = item },
                    )

                    if (!isLastItem) {
                        PrimalDivider()
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
private fun RecoverFollowListDialog(
    backup: FollowListBackup,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.premium_recover_contact_list_dialog_title),
            )
        },
        text = {
            val date = Instant.ofEpochSecond(backup.timestamp).formatToDefaultDateFormat(FormatStyle.MEDIUM)
            val followsCount = backup.followsCount.toString()
            Text(
                text = stringResource(R.string.premium_recover_contact_list_dialog_text, date, followsCount),
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.premium_recover_contact_list_dialog_dismiss_button),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.premium_recover_contact_list_dialog_confirm_button),
                )
            }
        },
    )
}
