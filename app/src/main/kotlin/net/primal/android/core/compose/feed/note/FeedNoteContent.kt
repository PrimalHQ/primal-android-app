package net.primal.android.core.compose.feed.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import net.primal.android.R
import net.primal.android.attachments.domain.calculateImageSize
import net.primal.android.attachments.domain.findNearestOrNull
import net.primal.android.core.compose.PostImageListItemImage
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.NoteContentUi
import net.primal.android.core.compose.feed.model.NoteNostrUriUi
import net.primal.android.core.compose.feed.model.asNoteNostrUriUi
import net.primal.android.core.utils.HashtagMatcher
import net.primal.android.core.utils.parseHashtags
import net.primal.android.feed.db.ReferencedPost
import net.primal.android.feed.db.ReferencedUser
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

private const val PROFILE_ID_ANNOTATION_TAG = "profileId"
private const val URL_ANNOTATION_TAG = "url"
private const val NOTE_ANNOTATION_TAG = "note"
private const val HASHTAG_ANNOTATION_TAG = "hashtag"

private fun List<NoteAttachmentUi>.filterImages() =
    filter {
        it.mimeType?.startsWith("image") == true
    }

private fun List<NoteAttachmentUi>.filterNotImages() =
    filterNot {
        it.mimeType?.startsWith("image") == true
    }

private fun List<NoteNostrUriUi>.filterReferencedPosts() =
    filter {
        it.referencedPost != null
    }

private fun List<NoteNostrUriUi>.filterReferencedUsers() =
    filter {
        it.referencedUser != null
    }

private fun String.withoutUrls(urls: List<String>): String {
    var newContent = this
    urls.forEach {
        newContent = newContent.replace(it, "")
    }
    return newContent
}

private fun String.replaceNostrProfileUrisWithHandles(resources: List<NoteNostrUriUi>): String {
    var newContent = this
    resources.forEach {
        checkNotNull(it.referencedUser)
        newContent = newContent.replace(
            oldValue = it.uri,
            newValue = it.referencedUser.displayUsername,
            ignoreCase = true,
        )
    }
    return newContent
}

private val noteLinkLeftovers = listOf(
    "https://primal.net/e/ " to "",
    "https://www.primal.net/e/ " to "",
    "http://primal.net/e/ " to "",
    "http://www.primal.net/e/ " to "",
    "https://primal.net/e/\n" to "",
    "https://www.primal.net/e/\n" to "",
    "http://primal.net/e/\n" to "",
    "http://www.primal.net/e/\n" to "",
)

private val profileLinkLeftovers = listOf(
    "https://primal.net/p/@" to "@",
    "https://www.primal.net/p/@" to "@",
    "http://primal.net/p/@" to "@",
    "http://www.primal.net/p/@" to "@",
)

private fun String.clearParsedPrimalLinks(): String {
    var newContent = this
    (noteLinkLeftovers + profileLinkLeftovers).forEach {
        newContent = newContent.replace(
            oldValue = it.first,
            newValue = it.second,
            ignoreCase = false,
        )
    }
    noteLinkLeftovers.map { it.first.trim() }.toSet().forEach {
        if (newContent.endsWith(it)) {
            newContent = newContent.replace(
                oldValue = it,
                newValue = "",
            )
        }
    }
    return newContent
}

private fun String.ellipsize(expanded: Boolean, ellipsizeText: String): String {
    val shouldEllipsize = length > 500
    return if (expanded || !shouldEllipsize) {
        this
    } else {
        substring(0, 500).plus(" $ellipsizeText")
    }
}

@Composable
fun FeedNoteContent(
    modifier: Modifier = Modifier,
    data: NoteContentUi,
    expanded: Boolean,
    onProfileClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onClick: (Offset) -> Unit,
    onUrlClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    highlightColor: Color = AppTheme.colorScheme.secondary,
    contentColor: Color = AppTheme.colorScheme.onSurface,
    referencedNoteContainerColor: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
) {
    val seeMoreText = stringResource(id = R.string.feed_see_more)
    val contentText = remember {
        renderContentAsAnnotatedString(
            data = data,
            expanded = expanded,
            seeMoreText = seeMoreText,
            highlightColor = highlightColor,
        )
    }

    Column(
        modifier = modifier,
    ) {
        if (contentText.isNotEmpty()) {
            PrimalClickableText(
                style = AppTheme.typography.bodyMedium.copy(
                    color = contentColor,
                    lineHeight = 20.sp,
                ),
                text = contentText,
                onClick = { position, offset ->
                    contentText.getStringAnnotations(
                        start = position,
                        end = position,
                    ).firstOrNull()?.let { annotation ->
                        when (annotation.tag) {
                            PROFILE_ID_ANNOTATION_TAG -> onProfileClick(annotation.item)
                            URL_ANNOTATION_TAG -> onUrlClick(annotation.item)
                            NOTE_ANNOTATION_TAG -> onPostClick(annotation.item)
                            HASHTAG_ANNOTATION_TAG -> onHashtagClick(annotation.item)
                        }
                    } ?: onClick(offset)
                },
            )
        }

        val imageAttachments = remember { data.attachments.filterImages() }
        if (imageAttachments.isNotEmpty()) {
            FeedNoteImages(imageAttachments = imageAttachments)
        }

        val referencedPostResources = remember { data.nostrUris.filterReferencedPosts() }
        if (referencedPostResources.isNotEmpty()) {
            FeedReferencedNotes(
                postResources = referencedPostResources,
                expanded = expanded,
                containerColor = referencedNoteContainerColor,
                onPostClick = onPostClick,
            )
        }
    }
}

fun renderContentAsAnnotatedString(
    data: NoteContentUi,
    expanded: Boolean,
    seeMoreText: String,
    highlightColor: Color,
    shouldKeepNostrNoteUris: Boolean = false,
): AnnotatedString {
    val imageUrlResources = data.attachments.filterImages()
    val otherUrlResources = data.attachments.filterNotImages()
    val referencedPostResources = data.nostrUris.filterReferencedPosts()
    val referencedUserResources = data.nostrUris.filterReferencedUsers()

    val refinedContent = data.content
        .withoutUrls(urls = imageUrlResources.map { it.url })
        .withoutUrls(
            urls = if (!shouldKeepNostrNoteUris) {
                referencedPostResources.map { it.uri }
            } else {
                emptyList()
            },
        )
        .ellipsize(expanded = expanded, ellipsizeText = seeMoreText)
        .replaceNostrProfileUrisWithHandles(resources = referencedUserResources)
        .clearParsedPrimalLinks()
        .trim()

    return buildAnnotatedString {
        append(refinedContent)

        if (refinedContent.endsWith(seeMoreText)) {
            addStyle(
                style = SpanStyle(color = highlightColor),
                start = refinedContent.length - seeMoreText.length,
                end = refinedContent.length,
            )
        }

        otherUrlResources.map { it.url }.forEach {
            val startIndex = refinedContent.indexOf(it)
            if (startIndex >= 0) {
                val endIndex = startIndex + it.length
                addStyle(
                    style = SpanStyle(color = highlightColor),
                    start = startIndex,
                    end = endIndex,
                )
                addStringAnnotation(
                    tag = URL_ANNOTATION_TAG,
                    annotation = it,
                    start = startIndex,
                    end = endIndex,
                )
            }
        }

        referencedUserResources.forEach {
            checkNotNull(it.referencedUser)
            val displayHandle = it.referencedUser.displayUsername
            val startIndex = refinedContent.indexOf(displayHandle)
            if (startIndex >= 0) {
                val endIndex = startIndex + displayHandle.length
                addStyle(
                    style = SpanStyle(color = highlightColor),
                    start = startIndex,
                    end = endIndex,
                )
                addStringAnnotation(
                    tag = PROFILE_ID_ANNOTATION_TAG,
                    annotation = it.referencedUser.userId,
                    start = startIndex,
                    end = endIndex,
                )
            }
        }

        HashtagMatcher(content = refinedContent, hashtags = data.hashtags).matches()
            .forEach {
                addStyle(
                    style = SpanStyle(color = highlightColor),
                    start = it.startIndex,
                    end = it.endIndex,
                )
                addStringAnnotation(
                    tag = HASHTAG_ANNOTATION_TAG,
                    annotation = it.value,
                    start = it.startIndex,
                    end = it.endIndex,
                )
            }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedNoteImages(imageAttachments: List<NoteAttachmentUi>) {
    BoxWithConstraints {
        val imageSizeDp = findImageSize(attachment = imageAttachments.first())
        val imagesCount = imageAttachments.size

        val pagerState = rememberPagerState { imagesCount }
        HorizontalPager(state = pagerState) {
            NoteImage(
                attachment = imageAttachments[it],
                imageSizeDp = imageSizeDp,
            )
        }

        if (imagesCount > 1) {
            Row(
                Modifier
                    .height(32.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(imagesCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        AppTheme.colorScheme.primary
                    } else {
                        AppTheme.colorScheme.onPrimary
                    }
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun NoteImage(attachment: NoteAttachmentUi, imageSizeDp: DpSize) {
    BoxWithConstraints(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(AppTheme.shapes.medium),
    ) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
        val variant = attachment.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
        val imageSource = variant?.mediaUrl ?: attachment.url
        PostImageListItemImage(
            source = imageSource,
            modifier = Modifier
                .width(imageSizeDp.width)
                .height(imageSizeDp.height),
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.findImageSize(attachment: NoteAttachmentUi): DpSize {
    val density = LocalDensity.current.density
    val maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() }
    val maxWidth = maxWidth.value.toInt()
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.77).toInt()
    val variant = attachment.variants.findNearestOrNull(maxWidthPx = maxWidthPx)
    return variant.calculateImageSize(
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        density = density,
    )
}

@Composable
fun FeedReferencedNotes(
    postResources: List<NoteNostrUriUi>,
    expanded: Boolean,
    containerColor: Color,
    onPostClick: (String) -> Unit,
) {
    val displayableNotes = if (postResources.isNotEmpty()) {
        if (expanded) postResources else postResources.subList(0, 1)
    } else {
        emptyList()
    }

    Column {
        displayableNotes.forEach { nostrResourceUi ->
            val data = nostrResourceUi.referencedPost
            checkNotNull(data)
            ReferencedNoteCard(
                modifier = Modifier.padding(vertical = 4.dp),
                data = FeedPostUi(
                    postId = data.postId,
                    repostId = null,
                    repostAuthorId = null,
                    repostAuthorName = null,
                    authorId = data.authorId,
                    authorName = data.authorName,
                    authorHandle = data.authorName,
                    authorInternetIdentifier = data.authorInternetIdentifier,
                    authorAvatarUrl = data.authorAvatarUrl,
                    authorAvatarVariants = emptyList(),
                    attachments = emptyList(),
                    nostrUris = data.nostrResources.map { it.asNoteNostrUriUi() },
                    timestamp = Instant.ofEpochSecond(data.createdAt),
                    content = data.content,
                    stats = FeedPostStatsUi(),
                    hashtags = data.content.parseHashtags(),
                    rawNostrEventJson = "",
                ),
                onPostClick = onPostClick,
                colors = CardDefaults.cardColors(containerColor = containerColor),
            )
        }
    }
}

@Preview
@Composable
fun PreviewPostContent() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        Surface {
            FeedNoteContent(
                data = NoteContentUi(
                    content = """
                        Unfortunately the days of using pseudonyms in metaspace are numbered. #nostr 
                        nostr:referencedUser
                    """.trimIndent(),
                    attachments = emptyList(),
                    nostrUris = listOf(
                        NoteNostrUriUi(
                            uri = "nostr:referencedUser",
                            referencedPost = null,
                            referencedUser = ReferencedUser(
                                userId = "nostr:referencedUser",
                                handle = "alex",
                            ),
                        ),
                    ),
                    hashtags = listOf("#nostr"),
                ),
                expanded = false,
                onProfileClick = {},
                onPostClick = {},
                onClick = {},
                onUrlClick = {},
                onHashtagClick = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewPostContentWithReferencedPost() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        Surface {
            FeedNoteContent(
                data = NoteContentUi(
                    content = """
                        Unfortunately the days of using pseudonyms in metaspace are numbered. #nostr
                        
                        nostr:referencedPost
                        
                        Or maybe not.
                        
                        nostr:referenced2Post
                    """.trimIndent(),
                    attachments = emptyList(),
                    nostrUris = listOf(
                        NoteNostrUriUi(
                            uri = "nostr:referencedPost",
                            referencedPost = ReferencedPost(
                                postId = "postId",
                                createdAt = 0,
                                content = "This is referenced post.",
                                authorId = "authorId",
                                authorName = "primal",
                                authorAvatarUrl = null,
                                authorAvatarVariants = emptyList(),
                                authorInternetIdentifier = "hi@primal.net",
                                authorLightningAddress = "h@getalby.com",
                                nostrResources = emptyList(),

                            ),
                            referencedUser = null,
                        ),
                        NoteNostrUriUi(
                            uri = "nostr:referenced2Post",
                            referencedPost = ReferencedPost(
                                postId = "postId",
                                createdAt = 0,
                                content = "This is referenced post #2.",
                                authorId = "authorId",
                                authorName = "primal",
                                authorAvatarUrl = null,
                                authorAvatarVariants = emptyList(),
                                authorInternetIdentifier = "hi@primal.net",
                                authorLightningAddress = "h@getalby.com",
                                nostrResources = emptyList(),
                            ),
                            referencedUser = null,
                        ),
                    ),
                    hashtags = listOf("#nostr"),
                ),
                expanded = false,
                onProfileClick = {},
                onPostClick = {},
                onClick = {},
                onUrlClick = {},
                onHashtagClick = {},
            )
        }
    }
}
