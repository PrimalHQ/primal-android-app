package net.primal.android.stream.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.ext.openUriSafely
import net.primal.android.profile.details.ui.ProfileActions
import net.primal.android.stream.LiveStreamContract
import net.primal.android.theme.AppTheme
import net.primal.core.utils.detectUrls
import net.primal.domain.nostr.utils.parseNostrUris

private const val URL_ANNOTATION_TAG = "url"
private const val HASHTAG_ANNOTATION_TAG = "hashtag"

@Composable
fun StreamInfoBottomSheet(
    modifier: Modifier = Modifier,
    activeUserId: String,
    streamInfo: LiveStreamContract.StreamInfoUi,
    isLive: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onZap: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        HostInfoAndActions(
            modifier = Modifier.padding(horizontal = 16.dp),
            onFollow = onFollow,
            onUnfollow = onUnfollow,
            onZap = onZap,
            onEditProfileClick = onEditProfileClick,
            onMessageClick = onMessageClick,
            onDrawerQrCodeClick = onDrawerQrCodeClick,
            activeUserId = activeUserId,
            streamInfo = streamInfo,
            isLive = isLive,
        )

        PrimalDivider(modifier = Modifier.padding(top = 16.dp))

        StreamDescriptionSection(
            streamInfo = streamInfo,
            isLive = isLive,
            onHashtagClick = onHashtagClick,
        )
    }
}

@Composable
private fun HostInfoAndActions(
    modifier: Modifier = Modifier,
    activeUserId: String,
    streamInfo: LiveStreamContract.StreamInfoUi,
    isLive: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onZap: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
) {
    val mainHostProfile = streamInfo.mainHostProfile!!
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UniversalAvatarThumbnail(
                isLive = isLive,
                avatarCdnImage = mainHostProfile.avatarCdnImage,
                avatarSize = 56.dp,
                legendaryCustomization = mainHostProfile.premiumDetails?.legendaryCustomization,
            )
            Column(modifier = Modifier.weight(1f)) {
                NostrUserText(
                    modifier = Modifier.padding(top = 4.dp),
                    displayName = streamInfo.mainHostProfile.userDisplayName,
                    internetIdentifier = streamInfo.mainHostProfile.internetIdentifier,
                    internetIdentifierBadgeSize = 20.dp,
                    internetIdentifierBadgeAlign = PlaceholderVerticalAlign.Center,
                    legendaryCustomization = streamInfo.mainHostProfile.premiumDetails?.legendaryCustomization,
                )
                streamInfo.mainHostProfileStats?.followersCount?.let {
                    Text(
                        text = stringResource(id = R.string.live_stream_followers_count, numberFormat.format(it)),
                        style = AppTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                        ),
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    )
                }
            }
        }

        ProfileActions(
            modifier = Modifier.fillMaxWidth(),
            isFollowed = streamInfo.isMainHostFollowedByActiveUser,
            isActiveUser = activeUserId == streamInfo.mainHostId,
            onEditProfileClick = onEditProfileClick,
            onMessageClick = { onMessageClick(streamInfo.mainHostId) },
            onZapProfileClick = onZap,
            onDrawerQrCodeClick = { onDrawerQrCodeClick(streamInfo.mainHostId) },
            onFollow = onFollow,
            onUnfollow = onUnfollow,
        )
    }
}

@Composable
private fun StreamDescriptionSection(
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
            .background(
                AppTheme.extraColorScheme.surfaceVariantAlt1,
            )
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
                textDecoration = TextDecoration.Underline,
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

private fun String.detectHashtags(): List<String> = Regex("""#\w+""").findAll(this).map { it.value }.toList()

private fun buildAnnotatedStringWithHighlights(
    text: String,
    urlStyle: SpanStyle,
    hashtagStyle: SpanStyle,
): AnnotatedString {
    val urls = text.detectUrls() + text.parseNostrUris()
    val hashtags = text.detectHashtags()

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
