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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedRepliesFilled
import net.primal.android.core.compose.icons.primaliconpack.Highlight
import net.primal.android.core.compose.icons.primaliconpack.Quote
import net.primal.android.core.compose.icons.primaliconpack.RemoveHighlight
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.highlights.model.CommentUi
import net.primal.android.highlights.model.JoinedHighlightsUi
import net.primal.android.theme.AppTheme

internal val DARK_THEME_ACTION_BUTTON_COLOR = Color(0xFF282828)

@Composable
fun HighlightActivityBottomSheetHandler(
    selectedHighlight: JoinedHighlightsUi?,
    dismissSelection: () -> Unit,
    isHighlighted: Boolean,
) {
    if (selectedHighlight != null) {
        HighlightActivityBottomSheet(
            onDismissRequest = dismissSelection,
            selectedHighlight = selectedHighlight,
            isHighlighted = isHighlighted,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightActivityBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    selectedHighlight: JoinedHighlightsUi,
    isHighlighted: Boolean,
) {
    ModalBottomSheet(
        tonalElevation = 0.dp,
        modifier = modifier.statusBarsPadding(),
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column {
            PrimalTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                    scrolledContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                ),
                title = stringResource(id = R.string.article_details_highlight_activity_title),
                textColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
            LazyColumn(
                state = rememberLazyListState(),
                modifier = Modifier.weight(weight = 1f, fill = false),
            ) {
                item {
                    HighlightAuthorsRow(authors = selectedHighlight.authors)
                }
                items(
                    items = selectedHighlight.comments,
                    key = { it.commentId },
                ) {
                    CommentRow(comment = it)
                }
            }
            HighlightActionButtons(
                onQuoteClick = {},
                onCommentClick = {},
                onToggleHighlightClick = {},
                isHighlighted = isHighlighted,
            )
        }
    }
}

@Composable
fun CommentRow(modifier: Modifier = Modifier, comment: CommentUi) {
    PrimalDivider()
    Column(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                UniversalAvatarThumbnail(
                    avatarCdnImage = comment.authorCdnImage,
                    legendaryCustomization = comment.authorLegendaryCustomization,
                    avatarSize = 28.dp,
                )
                NostrUserText(
                    modifier = Modifier.padding(start = 8.dp),
                    displayName = comment.authorDisplayName ?: "",
                    internetIdentifier = comment.authorInternetIdentifier,
                    customBadgeStyle = if (comment.authorLegendaryCustomization?.customBadge == true) {
                        comment.authorLegendaryCustomization.legendaryStyle
                    } else {
                        null
                    },
                )
                Text(
                    text = stringResource(id = R.string.article_details_highlight_activity_commented),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.onPrimary,
                )
            }
            Text(
                text = comment.createdAt.asBeforeNowFormat(),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
        Text(
            modifier = Modifier.padding(start = 58.dp, end = 12.dp),
            text = comment.content,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun HighlightActionButtons(
    modifier: Modifier = Modifier,
    isHighlighted: Boolean,
    onQuoteClick: () -> Unit,
    onCommentClick: () -> Unit,
    onToggleHighlightClick: () -> Unit,
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ActionButton(
            icon = PrimalIcons.Quote,
            onClick = onQuoteClick,
            text = stringResource(id = R.string.article_details_highlight_activity_quote),
        )
        ActionButton(
            icon = PrimalIcons.FeedRepliesFilled,
            onClick = onCommentClick,
            text = stringResource(id = R.string.article_details_highlight_activity_comment),
        )
        ActionButton(
            icon = if (isHighlighted) {
                PrimalIcons.RemoveHighlight
            } else {
                PrimalIcons.Highlight
            },
            onClick = onToggleHighlightClick,
            text = if (isHighlighted) {
                stringResource(id = R.string.article_details_highlight_activity_remove_highlight)
            } else {
                stringResource(id = R.string.article_details_highlight_activity_highlight)
            },
        )
    }
}

@Composable
private fun RowScope.ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {

    val isDarkTheme = isAppInDarkPrimalTheme()
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDarkTheme) {
                DARK_THEME_ACTION_BUTTON_COLOR
            } else {
                AppTheme.extraColorScheme.surfaceVariantAlt3
            },
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
fun HighlightAuthorsRow(authors: Set<ProfileDetailsUi>) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
            modifier = Modifier.padding(start = 56.dp),
            text = authors.map { it.authorDisplayName }.buildHighlightedString(),
            style = AppTheme.typography.bodyMedium,
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
