package net.primal.android.thread.articles.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AvatarOverlap
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedRepliesFilled
import net.primal.android.core.compose.icons.primaliconpack.Highlight
import net.primal.android.core.compose.icons.primaliconpack.Quote
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.highlights.model.JoinedHighlightsUi
import net.primal.android.theme.AppTheme


@Composable
fun HighlightActivityBottomSheetHandler(
    selectedHighlight: JoinedHighlightsUi?,
    dismissSelection: () -> Unit,
) {
    if (selectedHighlight != null) {
        HighlightActivityBottomSheet(
            onDismissRequest = dismissSelection,
            selectedHighlight = selectedHighlight,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightActivityBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    selectedHighlight: JoinedHighlightsUi,
) {
    ModalBottomSheet(
        tonalElevation = 0.dp,
        modifier = modifier.statusBarsPadding(),
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimalTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                    scrolledContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                ),
                title = "Highlight Activity",
                textColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
            Column {
                HighlightAuthorsRow(authors = selectedHighlight.authors)
            }
            HighlightActionButtons(
                onQuoteClick = {},
                onCommentClick = {},
                onToggleHighlightClick = {},
            )
        }
    }
}

@Composable
fun HighlightActionButtons(onQuoteClick: () -> Unit, onCommentClick: () -> Unit, onToggleHighlightClick: () -> Unit) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ActionButton(
            icon = PrimalIcons.Quote,
            onClick = onQuoteClick,
            text = "Quote",
        )
        ActionButton(
            icon = PrimalIcons.FeedRepliesFilled,
            onClick = onCommentClick,
            text = "Comment",
        )
        ActionButton(
            icon = PrimalIcons.Highlight,
            onClick = onToggleHighlightClick,
            text = "Highlight",
        )
    }
}

@Composable
private fun RowScope.ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF282828),
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        shape = AppTheme.shapes.medium,
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = icon,
                tint = AppTheme.colorScheme.onPrimary,
                contentDescription = null,
            )
            Text(
                text = text,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Visible,
            )
        }
    }
}

@Composable
fun HighlightAuthorsRow(
    authors: Set<ProfileDetailsUi>,
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = PrimalIcons.Highlight,
                contentDescription = null,
                tint = AppTheme.colorScheme.onPrimary,
            )
            AvatarThumbnailsRow(
                avatarCdnImages = authors.map { it.avatarCdnImage },
                avatarLegendaryCustomizations = authors.map { it.premiumDetails?.legendaryCustomization },
                avatarOverlap = AvatarOverlap.None,
                hasAvatarBorder = false,
                displayAvatarOverflowIndicator = true,
                maxAvatarsToShow = 6,
            )
        }
        Text(
            modifier = Modifier.padding(start = 60.dp),
            text = authors.map { it.authorDisplayName }.buildHighlightedString(),
        )
    }
}

@Composable
private fun List<String>.buildHighlightedString() =
    buildAnnotatedString {
        withStyle(style = SpanStyle(color = AppTheme.colorScheme.onPrimary)) {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(this@buildHighlightedString.first())
            }
            val otherSize = this@buildHighlightedString.size - 1
            if (otherSize == 0) {
                append(" " + stringResource(id = R.string.article_details_highlight_activity_authors_zero))
            } else {
                append(
                    " " +
                        pluralStringResource(
                            id = R.plurals.article_details_highlight_activity_authors,
                            count = otherSize,
                            otherSize,
                        ),
                )
            }
        }
    }
