package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NwcExternalAppConnection
import net.primal.android.core.compose.icons.primaliconpack.NwcExternalAppForeground
import net.primal.android.notes.feed.note.ui.attachment.NoteImageLoadingPlaceholder
import net.primal.android.theme.AppTheme

@Composable
fun WalletConnectionEditorHeader(
    modifier: Modifier = Modifier,
    appName: String? = null,
    appIcon: String? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(19.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                modifier = Modifier
                    .clip(AppTheme.shapes.medium)
                    .size(99.dp),
                painter = painterResource(id = R.drawable.primal_nwc_logo),
                contentDescription = stringResource(id = R.string.settings_wallet_nwc_primal_wallet),
                tint = Color.Unspecified,
            )

            Text(
                modifier = Modifier.padding(top = 13.dp),
                text = stringResource(id = R.string.settings_wallet_nwc_primal_wallet),
            )
        }

        Icon(
            modifier = Modifier.offset(y = (-13).dp),
            imageVector = PrimalIcons.NwcExternalAppConnection,
            contentDescription = "Connection",
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (appIcon.isNullOrEmpty()) {
                Icon(
                    modifier = Modifier
                        .clip(AppTheme.shapes.medium)
                        .background(color = Color(color = 0xFFE5E5E5))
                        .padding(21.dp)
                        .size(54.dp),
                    imageVector = PrimalIcons.NwcExternalAppForeground,
                    contentDescription = appName ?: stringResource(id = R.string.settings_wallet_nwc_external_app),
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                )
            } else {
                SubcomposeAsyncImage(
                    model = appIcon,
                    modifier = Modifier
                        .clip(AppTheme.shapes.medium)
                        .size(99.dp),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    loading = { NoteImageLoadingPlaceholder() },
                    error = {
                        Icon(
                            modifier = Modifier
                                .clip(AppTheme.shapes.medium)
                                .background(color = Color(color = 0xFFE5E5E5))
                                .padding(21.dp)
                                .size(54.dp),
                            imageVector = PrimalIcons.NwcExternalAppForeground,
                            contentDescription = appName
                                ?: stringResource(id = R.string.settings_wallet_nwc_external_app),
                            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                        )
                    },
                )
            }

            Text(
                modifier = Modifier.padding(top = 13.dp),
                text = appName ?: stringResource(id = R.string.settings_wallet_nwc_external_app),
            )
        }
    }
}
