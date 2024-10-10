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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.time.Instant
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.LightningBoltFilled
import net.primal.android.explore.api.model.ExploreZapData
import net.primal.android.theme.AppTheme

@Composable
fun ExploreZaps(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
) {
    val viewModel: ExploreZapsViewModel = hiltViewModel()
    val uiState = viewModel.state.collectAsState()

    ExploreZaps(
        modifier = modifier,
        paddingValues = paddingValues,
        onNoteClick = onNoteClick,
        onProfileClick = onProfileClick,
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
private fun ExploreZaps(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    state: ExploreZapsContract.UiState,
    eventPublisher: (ExploreZapsContract.UiEvent) -> Unit,
) {
    when {
        state.loading && state.zaps.isEmpty() -> {
            PrimalLoadingSpinner()
        }

        state.zaps.isEmpty() -> {
            ListNoContent(
                modifier = Modifier.fillMaxSize(),
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
                    key = { "${it.noteId}:${it.sender?.pubkey}:${it.createdAt.toEpochMilli()}" },
                ) { item ->
                    ZapListItem(
                        zapData = item,
                        onProfileClick = onProfileClick,
                        onNoteClick = onNoteClick,
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
    zapData: ExploreZapData,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.extraLarge)
            .clickable { onNoteClick(zapData.noteId) }
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
            .padding(all = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ZapHeader(
            onSenderAvatarClick = { zapData.sender?.pubkey?.let { onProfileClick(it) } },
            senderCdnImage = zapData.sender?.avatarCdnImage,
            amountSats = zapData.amountSats,
            message = zapData.zapMessage,
        )
        if (zapData.noteContent == null) {
            ProfileSummary(
                receiverDisplayName = zapData.receiver?.authorDisplayName,
                noteTimestamp = zapData.createdAt,
                receiverCdnResource = zapData.receiver?.avatarCdnImage,
                onReceiverAvatarClick = { zapData.receiver?.pubkey?.let { onProfileClick(it) } },
            )
        } else {
            NoteSummary(
                receiverDisplayName = zapData.receiver?.authorDisplayName,
                noteContent = zapData.noteContent,
                noteTimestamp = zapData.createdAt,
                receiverCdnResource = zapData.receiver?.avatarCdnImage,
                onReceiverAvatarClick = { zapData.receiver?.pubkey?.let { onProfileClick(it) } },
            )
        }
    }
}

@Composable
private fun NoteSummary(
    receiverDisplayName: String?,
    receiverCdnResource: CdnImage?,
    onReceiverAvatarClick: () -> Unit,
    noteContent: String,
    noteTimestamp: Instant,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AvatarThumbnail(
            avatarCdnImage = receiverCdnResource,
            avatarSize = 32.dp,
            onClick = onReceiverAvatarClick,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            receiverDisplayName?.let {
                ReceiverHeading(
                    receiverDisplayName = receiverDisplayName,
                    noteTimestamp = noteTimestamp,
                )
            }
            Text(
                text = noteContent.split("\n").filter { it.isNotBlank() }.joinToString(separator = " "),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun ProfileSummary(
    modifier: Modifier = Modifier,
    receiverDisplayName: String?,
    receiverCdnResource: CdnImage?,
    onReceiverAvatarClick: () -> Unit,
    noteTimestamp: Instant,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnail(
            avatarCdnImage = receiverCdnResource,
            avatarSize = 32.dp,
            onClick = onReceiverAvatarClick,
        )
        receiverDisplayName?.let {
            ReceiverHeading(
                receiverDisplayName = receiverDisplayName,
                noteTimestamp = noteTimestamp,
            )
        }
    }
}

@Composable
fun ReceiverHeading(receiverDisplayName: String, noteTimestamp: Instant) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 16.dp),
    ) {
        Text(
            modifier = Modifier.weight(weight = 1f, fill = false),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            fontWeight = FontWeight.Bold,
            text = receiverDisplayName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = AppTheme.typography.bodyMedium,
        )
        Text(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            text = " • ${noteTimestamp.asBeforeNowFormat()}",
            style = AppTheme.typography.bodyMedium,
        )
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
            .background(AppTheme.colorScheme.background)
            .padding(end = 16.dp)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnail(
            avatarCdnImage = senderCdnImage,
            avatarSize = 32.dp,
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
            )
        }
    }
}
