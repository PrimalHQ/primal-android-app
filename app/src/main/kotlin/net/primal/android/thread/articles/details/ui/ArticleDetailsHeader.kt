package net.primal.android.thread.articles.details.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.attachments.domain.findNearestOrNull
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme
import net.primal.android.thread.articles.details.ui.rendering.MarkdownRenderer

@Composable
fun ArticleDetailsHeader(
    title: String,
    date: Instant?,
    modifier: Modifier = Modifier,
    cover: CdnImage? = null,
    summary: String? = null,
) {
    Column(
        modifier = modifier,
    ) {
        if (date != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = date.formatDate(),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = title,
            style = AppTheme.typography.titleLarge.copy(
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.colorScheme.onSurface,
            ),
        )

        if (cover != null) {
            Spacer(modifier = Modifier.height(12.dp))
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = AppTheme.shapes.small),
            ) {
                val maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
                val variant = cover.variants.findNearestOrNull(maxWidthPx = maxWidthPx)

                SubcomposeAsyncImage(
                    modifier = Modifier.fillMaxWidth(),
                    model = variant?.mediaUrl ?: cover.sourceUrl,
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null,
                )
            }
        }

        if (summary != null) {
            val density = LocalDensity.current
            val lineColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3
            Spacer(modifier = Modifier.height(12.dp))
            MarkdownRenderer(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .padding(vertical = 4.dp)
                    .drawWithCache {
                        val startPadding = with(density) { 12.dp.toPx() }
                        val verticalPadding = with(density) { 4.dp.toPx() }
                        onDrawBehind {
                            drawLine(
                                color = lineColor,
                                start = Offset(
                                    x = -startPadding,
                                    y = -verticalPadding,
                                ),
                                end = Offset(
                                    x = -startPadding,
                                    y = size.height + verticalPadding / 2,
                                ),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }
                    },
                markdown = summary,
                showHighlights = false,
            )
        }
    }
}

@Composable
private fun Instant.formatDate(): String {
    val zoneId: ZoneId = ZoneId.systemDefault()
    val locale: Locale = Locale.getDefault()

    val formatter: DateTimeFormatter = DateTimeFormatter
        .ofPattern("MMM d, yyyy")
        .withLocale(locale)

    return formatter.format(this.atZone(zoneId).toLocalDate())
}

@Preview
@Composable
fun PreviewArticleDetailsHeader() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            ArticleDetailsHeader(
                modifier = Modifier.fillMaxWidth(),
                title = "Welcome to Article Header test",
                summary = """
                        This is a short summary of this preview test.
                        This is a short summary of this preview test.
                        This is a short summary of this preview test.
                        This is a short summary of this preview test.
                """.trimIndent(),
                date = Instant.now(),
            )
        }
    }
}
