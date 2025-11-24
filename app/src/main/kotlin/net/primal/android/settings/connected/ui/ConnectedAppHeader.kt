package net.primal.android.settings.connected.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.core.utils.PrimalDateFormats
import net.primal.android.core.utils.rememberPrimalFormattedDateTime
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

@Composable
fun ConnectedAppHeader(
    modifier: Modifier = Modifier,
    appName: String?,
    appIconUrl: String?,
    startedAt: Long? = null,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AppIconThumbnail(
                modifier = Modifier.padding(bottom = 6.dp),
                avatarCdnImage = appIconUrl?.let { CdnImage(it) },
                appName = appName,
                avatarSize = 48.dp,
            )

            Text(
                text = appName ?: stringResource(id = R.string.settings_connected_apps_unknown),
                style = AppTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onPrimary,
            )

            if (startedAt != null) {
                val formattedStartedAt = rememberPrimalFormattedDateTime(
                    timestamp = startedAt,
                    format = PrimalDateFormats.DATETIME_MM_DD_YYYY_HH_MM_A,
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = stringResource(id = R.string.settings_session_details_started_on, formattedStartedAt),
                    style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        }
    }
}
