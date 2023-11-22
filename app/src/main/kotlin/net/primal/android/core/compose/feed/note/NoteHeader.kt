package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import java.time.Instant
import kotlin.time.Duration.Companion.seconds
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun FeedNoteHeader(
    modifier: Modifier = Modifier,
    authorDisplayName: String,
    singleLine: Boolean = false,
    postTimestamp: Instant? = null,
    authorAvatarSize: Dp = 42.dp,
    authorAvatarVisible: Boolean = true,
    authorAvatarCdnImage: CdnImage? = null,
    authorInternetIdentifier: String? = null,
    onAuthorAvatarClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (authorAvatarVisible) {
            AvatarThumbnail(
                avatarCdnImage = authorAvatarCdnImage,
                avatarSize = authorAvatarSize,
                onClick = onAuthorAvatarClick,
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = if (authorAvatarVisible) 8.dp else 0.dp),
        ) {
            val identifier = if (!singleLine) "" else authorInternetIdentifier?.formatNip05Identifier() ?: ""
            val suffixText = buildAnnotatedString {
                append(
                    AnnotatedString(
                        text = identifier,
                        spanStyle = SpanStyle(
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                            fontStyle = AppTheme.typography.bodySmall.fontStyle,
                        ),
                    ),
                )
            }

            ConstraintLayout(
                modifier = Modifier.wrapContentWidth(),
            ) {
                val (mainRef, endRef) = createRefs()

                NostrUserText(
                    modifier = Modifier
                        .constrainAs(mainRef) {
                            start.linkTo(parent.start)
                            end.linkTo(endRef.start)
                            width = Dimension.preferredWrapContent
                        },
                    displayName = authorDisplayName,
                    internetIdentifier = authorInternetIdentifier,
                    annotatedStringSuffixBuilder = {
                        append(suffixText)
                    },
                    style = AppTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    modifier = Modifier
                        .constrainAs(endRef) {
                            end.linkTo(parent.end)
                            start.linkTo(mainRef.end)
                            top.linkTo(mainRef.top)
                            bottom.linkTo(mainRef.bottom)
                            height = Dimension.fillToConstraints
                        }
                        .padding(top = 2.dp),
                    text = " • ${postTimestamp?.asBeforeNowFormat().orEmpty()}",
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }

            if (!authorInternetIdentifier.isNullOrEmpty() && !singleLine) {
                Text(
                    text = authorInternetIdentifier,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewLightNoteHeader() {
    PrimalTheme(
        primalTheme = PrimalTheme.Sunrise,
    ) {
        Surface {
            FeedNoteHeader(
                modifier = Modifier.fillMaxWidth(),
                authorDisplayName = "Donald Duck",
                postTimestamp = Instant.now().minusSeconds(3600.seconds.inWholeSeconds),
                authorInternetIdentifier = "donald@the.duck",
                onAuthorAvatarClick = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewLightSingleNoteHeader() {
    PrimalTheme(
        primalTheme = PrimalTheme.Sunrise,
    ) {
        Surface {
            FeedNoteHeader(
                modifier = Modifier.fillMaxWidth(),
                authorDisplayName = "Donald Duck",
                postTimestamp = Instant.now().minusSeconds(3600.seconds.inWholeSeconds),
                singleLine = true,
                authorInternetIdentifier = "donald@the.duck",
                onAuthorAvatarClick = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewDarkNoteHeader() {
    PrimalTheme(
        primalTheme = PrimalTheme.Sunset,
    ) {
        Surface {
            FeedNoteHeader(
                modifier = Modifier.fillMaxWidth(),
                authorDisplayName = "Donald Duck",
                postTimestamp = Instant.now().minusSeconds(3600.seconds.inWholeSeconds),
                authorAvatarVisible = false,
                authorInternetIdentifier = "donald@the.duck",
                onAuthorAvatarClick = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewDarkSingleLineNoAvatarNoteHeader() {
    PrimalTheme(
        primalTheme = PrimalTheme.Sunset,
    ) {
        Surface {
            FeedNoteHeader(
                modifier = Modifier.fillMaxWidth(),
                authorDisplayName = "Donald Duck",
                postTimestamp = Instant.now().minusSeconds(3600.seconds.inWholeSeconds),
                singleLine = true,
                authorAvatarVisible = false,
                authorInternetIdentifier = "donald@the.duck",
                onAuthorAvatarClick = {},
            )
        }
    }
}
