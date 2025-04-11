package net.primal.android.premium.leaderboard.ogs.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.format.FormatStyle
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.theme.AppTheme
import net.primal.domain.premium.OGLeaderboardEntry

@Composable
fun OGLeaderboardItem(
    item: OGLeaderboardEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ogSince = item.premiumSince?.let { Instant.ofEpochSecond(it) }

    ListItem(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        leadingContent = {
            UniversalAvatarThumbnail(
                avatarSize = 42.dp,
                avatarCdnImage = item.avatarCdnImage,
            )
        },
        headlineContent = {
            DisplayNameAndFirstCohortRow(
                firstCohort = item.firstCohort,
                displayName = item.displayName,
                internetIdentifier = item.internetIdentifier,
            )
        },
        supportingContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (ogSince == null) Arrangement.End else Arrangement.SpaceBetween,
            ) {
                ogSince?.let {
                    Text(
                        text = stringResource(id = R.string.premium_legend_leaderboard_since) +
                            ": " + ogSince.formatToDefaultDateFormat(FormatStyle.MEDIUM),
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    )
                }
                item.secondCohort?.let { secondCohort ->
                    Text(
                        text = secondCohort,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    )
                }
            }
        },
    )
    PrimalDivider()
}

@Composable
private fun DisplayNameAndFirstCohortRow(
    displayName: String?,
    internetIdentifier: String?,
    firstCohort: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        displayName?.let {
            NostrUserText(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                displayName = displayName,
                internetIdentifier = internetIdentifier,
                internetIdentifierBadgeAlign = PlaceholderVerticalAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        firstCohort?.let {
            Text(
                text = firstCohort,
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}
