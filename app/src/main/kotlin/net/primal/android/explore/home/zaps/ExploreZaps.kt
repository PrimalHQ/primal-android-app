package net.primal.android.explore.home.zaps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.time.Instant
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.LightningBoltFilled
import net.primal.android.explore.home.zaps.ui.ExploreZapNoteUi
import net.primal.android.notes.feed.model.NoteContentUi
import net.primal.android.notes.feed.note.ui.NoteContent
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme

@Composable
fun ExploreZaps(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    noteCallbacks: NoteCallbacks,
) {
    val viewModel: ExploreZapsViewModel = hiltViewModel()
    val uiState = viewModel.state.collectAsState()

    ExploreZaps(
        modifier = modifier,
        state = uiState.value,
        paddingValues = paddingValues,
        noteCallbacks = noteCallbacks,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
private fun ExploreZaps(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    noteCallbacks: NoteCallbacks,
    state: ExploreZapsContract.UiState,
    eventPublisher: (ExploreZapsContract.UiEvent) -> Unit,
) {
    when {
        state.loading && state.zaps.isEmpty() -> {
            HeightAdjustableLoadingLazyListPlaceholder(
                modifier = modifier.fillMaxSize(),
                contentPaddingValues = paddingValues,
                clipShape = AppTheme.shapes.large,
                height = 112.dp,
            )
        }

        state.zaps.isEmpty() -> {
            ListNoContent(
                modifier = modifier.fillMaxSize(),
                noContentText = stringResource(id = R.string.explore_trending_zaps_no_content),
                refreshButtonVisible = true,
                onRefresh = { eventPublisher(ExploreZapsContract.UiEvent.RefreshZaps) },
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = paddingValues,
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(
                    items = state.zaps,
                    key = { "${it.noteContentUi.noteId}:${it.sender?.pubkey}:${it.createdAt.toEpochMilli()}" },
                ) { item ->
                    ZapListItem(
                        zapData = item,
                        noteCallbacks = noteCallbacks,
                    )
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }
}

@Composable
fun ZapListItem(
    modifier: Modifier = Modifier,
    zapData: ExploreZapNoteUi,
    noteCallbacks: NoteCallbacks,
) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.extraLarge)
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = { noteCallbacks.onNoteClick?.invoke(zapData.noteContentUi.noteId) },
            )
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
            .padding(all = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ZapHeader(
            onSenderAvatarClick = { zapData.sender?.pubkey?.let { noteCallbacks.onProfileClick?.invoke(it) } },
            senderCdnImage = zapData.sender?.avatarCdnImage,
            amountSats = zapData.amountSats,
            message = zapData.zapMessage,
        )
        NoteSummary(
            noteContent = zapData.noteContentUi,
            noteTimestamp = zapData.createdAt,
            noteCallbacks = noteCallbacks,
            onNoteClick = { noteCallbacks.onNoteClick?.invoke(it) },
            receiverCdnResource = zapData.receiver?.avatarCdnImage,
            receiverDisplayName = zapData.receiver?.authorDisplayName,
            onReceiverAvatarClick = { zapData.receiver?.pubkey?.let { noteCallbacks.onProfileClick?.invoke(it) } },
        )
    }
}

@Composable
private fun NoteSummary(
    receiverDisplayName: String?,
    noteContent: NoteContentUi,
    receiverCdnResource: CdnImage?,
    noteTimestamp: Instant,
    noteCallbacks: NoteCallbacks,
    onReceiverAvatarClick: () -> Unit,
    onNoteClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 2.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AvatarThumbnail(
            avatarCdnImage = receiverCdnResource,
            avatarSize = 38.dp,
            onClick = onReceiverAvatarClick,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            receiverDisplayName?.let {
                Row {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                                    fontWeight = FontWeight.Bold,
                                ),
                            ) {
                                append(receiverDisplayName)
                            }
                            withStyle(style = SpanStyle(color = AppTheme.extraColorScheme.onSurfaceVariantAlt3)) {
                                append(" • ")
                                append(noteTimestamp.asBeforeNowFormat())
                            }
                        },
                        maxLines = 1,
                        style = AppTheme.typography.bodyMedium,
                    )
                }
            }
            NoteContent(
                expanded = false,
                noteCallbacks = noteCallbacks.copy(
                    onProfileClick = null,
                ),
                data = noteContent,
                contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                highlightColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                onClick = { onNoteClick(noteContent.noteId) },
            )
        }
    }
}

@Composable
private fun ZapHeader(
    onSenderAvatarClick: () -> Unit,
    senderCdnImage: CdnImage?,
    amountSats: ULong,
    message: String?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 100))
            .background(AppTheme.colorScheme.surfaceVariant)
            .padding(end = 16.dp)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnail(
            avatarCdnImage = senderCdnImage,
            avatarSize = 38.dp,
            onClick = onSenderAvatarClick,
        )
        val numberFormat = NumberFormat.getNumberInstance()
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = PrimalIcons.LightningBoltFilled,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AppTheme.colorScheme.onPrimary,
            )
            Text(
                text = numberFormat.format(amountSats.toLong()),
                fontWeight = FontWeight.ExtraBold,
                style = AppTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!message.isNullOrEmpty()) {
            Text(
                text = message,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        }
    }
}
