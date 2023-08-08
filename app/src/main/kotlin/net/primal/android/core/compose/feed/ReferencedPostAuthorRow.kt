package net.primal.android.core.compose.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.AvatarThumbnailListItemImage
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.media.model.MediaResourceUi
import net.primal.android.core.ext.findByUrl
import net.primal.android.core.utils.asBeforeNowFormat
import net.primal.android.core.utils.isPrimalIdentifier
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

@Composable
fun ReferencedPostAuthorRow(
    authorDisplayName: String,
    postTimestamp: Instant,
    authorAvatarUrl: String? = null,
    authorResources: List<MediaResourceUi> = emptyList(),
    authorInternetIdentifier: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val resource = authorResources.findByUrl(url = authorAvatarUrl)
        val variant = resource?.variants?.minByOrNull { it.width }
        val imageSource = variant?.mediaUrl ?: authorAvatarUrl

        AvatarThumbnailListItemImage(
            source = imageSource,
            size = 28.dp,
            hasBorder = authorInternetIdentifier.isPrimalIdentifier(),
        )

        val identifierTextColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2
        val identifierTextStyle = AppTheme.typography.bodyMedium
        NostrUserText(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
            displayName = authorDisplayName,
            internetIdentifier = authorInternetIdentifier,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            annotatedStringSuffixBuilder = {
                if (!authorInternetIdentifier.isNullOrEmpty()) {
                    append(
                        AnnotatedString(
                            text = authorInternetIdentifier,
                            spanStyle = SpanStyle(
                                color = identifierTextColor,
                                fontStyle = identifierTextStyle.fontStyle,
                            )
                        )
                    )
                }
            }
        )

        Text(
            text = buildAnnotatedString {
                val time = postTimestamp.asBeforeNowFormat(res = LocalContext.current.resources)
                append(
                    AnnotatedString(
                        text = " | $time",
                        spanStyle = SpanStyle(
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                            fontStyle = AppTheme.typography.bodyMedium.fontStyle,
                        )
                    )
                )
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
fun PreviewLightReferencedPostAuthorRow() {
    PrimalTheme(
        theme = PrimalTheme.Sunrise,
    ) {
        Surface {
            ReferencedPostAuthorRow(
                authorDisplayName = "Donald Duck",
                postTimestamp = Instant.now().minusSeconds(3600.seconds.inWholeSeconds),
                authorAvatarUrl = null,
                authorResources = emptyList(),
                authorInternetIdentifier = "duck.donald@primal.net",
            )
        }
    }
}

@Preview
@Composable
fun PreviewDarkReferencedPostAuthorRow() {
    PrimalTheme(
        theme = PrimalTheme.Sunset,
    ) {
        Surface {
            ReferencedPostAuthorRow(
                authorDisplayName = "Donald Duck",
                postTimestamp = Instant.now().minusSeconds(3600.seconds.inWholeSeconds),
                authorAvatarUrl = null,
                authorResources = emptyList(),
                authorInternetIdentifier = null,
            )
        }
    }
}
