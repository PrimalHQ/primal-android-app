package net.primal.android.stream.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.ConfirmActionAlertDialog
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.dropdown.DropdownPrimalMenuItem
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteId
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteLink
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyRawData
import net.primal.android.core.compose.icons.primaliconpack.ContextMuteUser
import net.primal.android.core.compose.icons.primaliconpack.ContextReportContent
import net.primal.android.core.compose.icons.primaliconpack.ContextShare
import net.primal.android.core.compose.icons.primaliconpack.Delete
import net.primal.android.core.compose.icons.primaliconpack.Quote
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.resolvePrimalStreamLink
import net.primal.android.core.utils.systemShareText
import net.primal.android.profile.report.ReportUserDialog
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.ReportType
import net.primal.domain.nostr.utils.withNostrPrefix

@ExperimentalMaterial3Api
@Composable
fun LiveStreamMenu(
    modifier: Modifier,
    naddr: Naddr,
    primalName: String?,
    isMainHostMuted: Boolean,
    isActiveUserMainHost: Boolean,
    rawNostrEvent: String?,
    menuVisible: Boolean,
    onMenuVisibilityChange: (Boolean) -> Unit,
    onQuoteClick: (String) -> Unit,
    onMuteUserClick: () -> Unit,
    onUnmuteUserClick: () -> Unit,
    onReportContentClick: (ReportType) -> Unit,
    onRequestDeleteClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    var reportDialogVisible by remember { mutableStateOf(false) }
    var deleteDialogVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onMenuVisibilityChange(true) },
        ),
    ) {
        icon()

        if (menuVisible) {
            MenuContent(
                naddr = naddr,
                primalName = primalName,
                isMuted = isMainHostMuted,
                isStreamAuthor = isActiveUserMainHost,
                rawNostrEvent = rawNostrEvent,
                onDismiss = { onMenuVisibilityChange(false) },
                onQuoteClick = onQuoteClick,
                onMuteUserClick = onMuteUserClick,
                onUnmuteUserClick = onUnmuteUserClick,
                onReportContentClick = { reportDialogVisible = true },
                onRequestDeleteClick = { deleteDialogVisible = true },
            )
        }
    }

    if (reportDialogVisible) {
        ReportUserDialog(
            onDismissRequest = { reportDialogVisible = false },
            onReportClick = { type ->
                reportDialogVisible = false
                onReportContentClick(type)
            },
        )
    }

    if (deleteDialogVisible) {
        ConfirmActionAlertDialog(
            confirmText = stringResource(id = R.string.live_stream_menu_delete_dialog_confirm),
            dismissText = stringResource(id = R.string.live_stream_menu_delete_dialog_cancel),
            dialogTitle = stringResource(id = R.string.live_stream_menu_delete_dialog_title),
            dialogText = stringResource(id = R.string.live_stream_menu_delete_dialog_text),
            onConfirmation = {
                deleteDialogVisible = false
                onRequestDeleteClick()
            },
            onDismissRequest = { deleteDialogVisible = false },
        )
    }
}

@Composable
private fun MenuContent(
    naddr: Naddr,
    primalName: String?,
    isMuted: Boolean,
    isStreamAuthor: Boolean,
    rawNostrEvent: String?,
    onDismiss: () -> Unit,
    onQuoteClick: (String) -> Unit,
    onMuteUserClick: () -> Unit,
    onUnmuteUserClick: () -> Unit,
    onReportContentClick: () -> Unit,
    onRequestDeleteClick: () -> Unit,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val copyConfirmationText = stringResource(id = R.string.live_stream_menu_copied)
    val streamLink = remember(naddr, primalName) {
        resolvePrimalStreamLink(naddr = naddr, primalName = primalName)
    }
    val naddrString = naddr.toNaddrString()

    fun dismissAnd(action: () -> Unit) {
        action()
        onDismiss()
    }

    fun dismissAndCopyWithToast(text: String) {
        copyText(context = context, text = text)
        uiScope.launch { Toast.makeText(context, copyConfirmationText, Toast.LENGTH_SHORT).show() }
        onDismiss()
    }

    DropdownPrimalMenu(
        expanded = true,
        onDismissRequest = onDismiss,
    ) {
        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.Quote,
            text = stringResource(id = R.string.live_stream_menu_quote_stream),
            onClick = { dismissAnd { onQuoteClick(naddrString) } },
        )
        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.ContextShare,
            text = stringResource(id = R.string.live_stream_menu_share_stream),
            onClick = { dismissAnd { systemShareText(context = context, text = streamLink) } },
        )
        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.ContextCopyNoteLink,
            text = stringResource(id = R.string.live_stream_menu_copy_stream_link),
            onClick = { dismissAndCopyWithToast(streamLink) },
        )
        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.ContextCopyNoteId,
            text = stringResource(id = R.string.live_stream_menu_copy_stream_id),
            onClick = { dismissAndCopyWithToast(naddrString.withNostrPrefix()) },
        )
        if (rawNostrEvent != null) {
            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextCopyRawData,
                text = stringResource(id = R.string.live_stream_menu_copy_raw_data),
                onClick = { dismissAndCopyWithToast(rawNostrEvent) },
            )
        }
        if (!isStreamAuthor) {
            AuthorActions(
                isMuted = isMuted,
                onMute = { dismissAnd { onMuteUserClick() } },
                onUnmute = { dismissAnd { onUnmuteUserClick() } },
                onReport = { dismissAnd { onReportContentClick() } },
            )
        }
        if (isStreamAuthor) {
            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.Delete,
                tint = AppTheme.colorScheme.error,
                text = stringResource(id = R.string.live_stream_menu_request_delete),
                onClick = { dismissAnd { onRequestDeleteClick() } },
            )
        }
    }
}

@Composable
private fun AuthorActions(
    isMuted: Boolean,
    onMute: () -> Unit,
    onUnmute: () -> Unit,
    onReport: () -> Unit,
) {
    DropdownPrimalMenuItem(
        trailingIconVector = PrimalIcons.ContextMuteUser,
        tint = AppTheme.colorScheme.error,
        text = if (isMuted) {
            stringResource(id = R.string.live_stream_menu_unmute_user)
        } else {
            stringResource(id = R.string.live_stream_menu_mute_user)
        },
        onClick = if (isMuted) onUnmute else onMute,
    )
    DropdownPrimalMenuItem(
        trailingIconVector = PrimalIcons.ContextReportContent,
        tint = AppTheme.colorScheme.error,
        text = stringResource(id = R.string.live_stream_menu_report_content),
        onClick = onReport,
    )
}
