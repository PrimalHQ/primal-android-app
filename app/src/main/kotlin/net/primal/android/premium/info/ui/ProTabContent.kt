package net.primal.android.premium.info.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.ext.openUriSafely
import net.primal.android.theme.AppTheme

private val UrlColor = Color(0xFFE47C00)

@Composable
fun ProTabContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(30.dp),
    ) {
        Text(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            fontSize = 18.sp,
            textAlign = TextAlign.Justify,
            text = stringResource(id = R.string.premium_more_info_why_pro),
        )
        ProFeatureRow(
            title = stringResource(id = R.string.premium_more_info_pro_feature_primal_studio_title),
            featureText = stringResource(id = R.string.premium_more_info_pro_feature_primal_studio),
            trailingUrl = stringResource(id = R.string.premium_more_info_pro_feature_primal_studio_url),
            painter = painterResource(R.drawable.primal_gold_wave_logo),
        )
        ProFeatureRow(
            title = stringResource(id = R.string.premium_more_info_pro_feature_primal_studio_title),
            featureText = stringResource(id = R.string.premium_more_info_pro_feature_legend_status),
            painter = painterResource(R.drawable.legend_profile_ring),
        )
    }
}

@Composable
private fun ProFeatureRow(
    title: String,
    featureText: String,
    painter: Painter,
    modifier: Modifier = Modifier,
    trailingUrl: String? = null,
) {
    val uriHandler = LocalUriHandler.current
    val textStyle = AppTheme.typography.bodyLarge.copy(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        textAlign = TextAlign.Left,
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(85.dp),
        )

        Column {
            Text(
                text = title,
                style = textStyle,
                color = AppTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = featureText,
                style = textStyle,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )

            if (!trailingUrl.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))

                UrlClickableText(
                    url = trailingUrl,
                    style = textStyle,
                    onClick = { uriHandler.openUriSafely(trailingUrl) },
                )
            }
        }
    }
}

@Composable
private fun UrlClickableText(
    url: String,
    style: TextStyle,
    onClick: () -> Unit,
) {
    Text(
        text = url,
        modifier = Modifier.clickable(onClick = onClick),
        color = UrlColor,
        style = style,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
