package net.primal.android.core.compose.zaps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.time.Instant
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.LightningBoltFilled
import net.primal.android.notes.feed.model.NoteContentUi
import net.primal.android.notes.feed.note.ui.NoteContent
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.theme.AppTheme

@Composable
fun ZapItem(
    senderId: String?,
    receiverId: String?,
    noteContentUi: NoteContentUi,
    amountInSats: ULong,
    createdAt: Instant,
    noteCallbacks: NoteCallbacks,
    message: String?,
    receiverDisplayName: String?,
    modifier: Modifier = Modifier,
    senderAvatarCdnImage: CdnImage? = null,
    senderLegendaryCustomization: LegendaryCustomization? = null,
    receiverAvatarCdnImage: CdnImage? = null,
    receiverLegendaryCustomization: LegendaryCustomization? = null,
) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.extraLarge)
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = { noteCallbacks.onNoteClick?.invoke(noteContentUi.noteId) },
            )
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
            .padding(all = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ZapHeader(
            onSenderAvatarClick = { senderId?.let { noteCallbacks.onProfileClick?.invoke(senderId) } },
            senderCdnImage = senderAvatarCdnImage,
            amountSats = amountInSats,
            message = message,
            senderLegendaryCustomization = senderLegendaryCustomization,
        )
        NoteSummary(
            noteContent = noteContentUi,
            noteTimestamp = createdAt,
            noteCallbacks = noteCallbacks,
            onNoteClick = { noteCallbacks.onNoteClick?.invoke(it) },
            receiverCdnResource = receiverAvatarCdnImage,
            receiverDisplayName = receiverDisplayName,
            onReceiverAvatarClick = { receiverId?.let { noteCallbacks.onProfileClick?.invoke(receiverId) } },
            receiverLegendaryCustomization = receiverLegendaryCustomization,
        )
    }
}

@Composable
private fun NoteSummary(
    receiverDisplayName: String?,
    noteContent: NoteContentUi,
    receiverCdnResource: CdnImage?,
    receiverLegendaryCustomization: LegendaryCustomization?,
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
        UniversalAvatarThumbnail(
            avatarCdnImage = receiverCdnResource,
            avatarSize = 38.dp,
            onClick = onReceiverAvatarClick,
            legendaryCustomization = receiverLegendaryCustomization,
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
    senderLegendaryCustomization: LegendaryCustomization?,
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
        UniversalAvatarThumbnail(
            avatarCdnImage = senderCdnImage,
            avatarSize = 38.dp,
            onClick = onSenderAvatarClick,
            legendaryCustomization = senderLegendaryCustomization,
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
