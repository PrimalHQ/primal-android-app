package net.primal.android.stream

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.theme.AppTheme

@Composable
fun StreamInfoSection(
    title: String,
    authorProfile: ProfileDetailsUi,
    viewers: Int,
    startedAt: Long?,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = AppTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (startedAt != null) {
                Text(
                    text = stringResource(
                        id = R.string.live_stream_started_at,
                        Instant.ofEpochSecond(startedAt).asBeforeNowFormat(),
                    ),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodyMedium,
                )
                Text(text = "|", color = AppTheme.extraColorScheme.onSurfaceVariantAlt2)
            }
            Text(
                text = stringResource(id = R.string.live_stream_viewers_count, numberFormat.format(viewers)),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                style = AppTheme.typography.bodyMedium,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UniversalAvatarThumbnail(
                    avatarCdnImage = authorProfile.avatarCdnImage,
                    avatarSize = 48.dp,
                )
                NostrUserText(
                    displayName = authorProfile.authorDisplayName,
                    internetIdentifier = authorProfile.internetIdentifier,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
