package net.primal.android.stream.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.utils.parseHashtags
import net.primal.android.stream.LiveStreamContract
import net.primal.android.theme.AppTheme
import net.primal.core.utils.detectUrls
import net.primal.domain.nostr.utils.parseNostrUris

private const val URL_ANNOTATION_TAG = "url"
private const val HASHTAG_ANNOTATION_TAG = "hashtag"

@Composable
fun StreamDescriptionSection(
    modifier: Modifier = Modifier,
    streamInfo: LiveStreamContract.StreamInfoUi,
    isLive: Boolean,
    onHashtagClick: (String) -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (streamInfo.title.isNotEmpty()) {
                Text(
                    text = streamInfo.title,
                    style = AppTheme.typography.titleLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 23.sp,
                        color = AppTheme.colorScheme.onSurface,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            StreamMetaData(
                isLive = isLive,
                startedAt = streamInfo.startedAt,
                viewers = streamInfo.viewers,
            )
        }

        if (!streamInfo.description.isNullOrEmpty()) {
            val urlStyle = SpanStyle(
                color = AppTheme.colorScheme.secondary,
            )
            val hashtagStyle = SpanStyle(
                color = AppTheme.colorScheme.secondary,
            )

            val annotatedDescription = remember {
                buildAnnotatedStringWithHighlights(
                    text = streamInfo.description,
                    urlStyle = urlStyle,
                    hashtagStyle = hashtagStyle,
                )
            }

            PrimalClickableText(
                text = annotatedDescription,
                style = AppTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                ),
                onClick = { position, _ ->
                    val annotations = annotatedDescription.getStringAnnotations(
                        start = position,
                        end = position,
                    )

                    annotations.firstOrNull { it.tag == URL_ANNOTATION_TAG }?.let {
                        uriHandler.openUriSafely(it.item)
                        return@PrimalClickableText
                    }

                    annotations.firstOrNull { it.tag == HASHTAG_ANNOTATION_TAG }?.let {
                        val hashtag = it.item.substring(1)
                        onHashtagClick(hashtag)
                    }
                },
            )
        }
    }
}

private fun buildAnnotatedStringWithHighlights(
    text: String,
    urlStyle: SpanStyle,
    hashtagStyle: SpanStyle,
): AnnotatedString {
    val urls = text.detectUrls() + text.parseNostrUris()
    val hashtags = text.parseHashtags()

    return buildAnnotatedString {
        append(text)

        urls.forEach { url ->
            var startIndex = text.indexOf(url)
            while (startIndex != -1) {
                val endIndex = startIndex + url.length
                addStyle(
                    style = urlStyle,
                    start = startIndex,
                    end = endIndex,
                )
                addStringAnnotation(
                    tag = URL_ANNOTATION_TAG,
                    annotation = url,
                    start = startIndex,
                    end = endIndex,
                )
                startIndex = text.indexOf(url, startIndex + 1)
            }
        }

        hashtags.forEach { hashtag ->
            var startIndex = text.indexOf(hashtag)
            while (startIndex != -1) {
                val endIndex = startIndex + hashtag.length
                addStyle(
                    style = hashtagStyle,
                    start = startIndex,
                    end = endIndex,
                )
                addStringAnnotation(
                    tag = HASHTAG_ANNOTATION_TAG,
                    annotation = hashtag,
                    start = startIndex,
                    end = endIndex,
                )
                startIndex = text.indexOf(hashtag, startIndex + 1)
            }
        }
    }
}
