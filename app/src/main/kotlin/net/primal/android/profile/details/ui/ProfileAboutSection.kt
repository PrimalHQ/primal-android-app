package net.primal.android.profile.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.utils.TextMatcher
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.nostr.utils.isNostrUri

private const val PROFILE_ID_ANNOTATION_TAG = "profileId"
private const val URL_ANNOTATION_TAG = "url"
private const val HASHTAG_ANNOTATION_TAG = "hashtag"

@Composable
fun ProfileAboutSection(
    modifier: Modifier = Modifier,
    about: String,
    aboutHashtags: List<String>,
    aboutUris: List<String>,
    referencedUsers: Set<ProfileDetailsUi>,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onUrlClick: (String) -> Unit,
) {
    val contentText = renderTextAsAnnotatedString(
        text = about,
        allUris = aboutUris,
        referencedUsers = referencedUsers,
        hashtags = aboutHashtags,
        highlightColor = AppTheme.colorScheme.secondary,
    )

    PrimalClickableText(
        modifier = modifier,
        style = AppTheme.typography.bodyMedium.copy(
            color = AppTheme.colorScheme.onSurface,
        ),
        text = contentText,
        onClick = { position, _ ->
            contentText.getStringAnnotations(
                start = position,
                end = position,
            ).firstOrNull()?.let { annotation ->
                when (annotation.tag) {
                    PROFILE_ID_ANNOTATION_TAG -> onProfileClick(annotation.item)
                    URL_ANNOTATION_TAG -> onUrlClick(annotation.item)
                    HASHTAG_ANNOTATION_TAG -> onHashtagClick(annotation.item)
                }
            }
        },
    )
}

fun renderTextAsAnnotatedString(
    text: String,
    allUris: List<String>,
    hashtags: List<String>,
    referencedUsers: Set<ProfileDetailsUi>,
    highlightColor: Color,
): AnnotatedString {
    val refinedContent = text
        .replaceNostrProfileUrisWithHandles(
            allUris = allUris,
            referencedUsers = referencedUsers,
        )
        .trim()

    return buildAnnotatedString {
        append(refinedContent)

        referencedUsers.forEach {
            val displayHandle = "@${it.userDisplayName}"
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
                    annotation = it.pubkey,
                    start = startIndex,
                    end = endIndex,
                )
            }
        }

        allUris.filterNot { it.isNostrUri() }.forEach {
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

        TextMatcher(content = refinedContent, texts = hashtags).matches()
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

private fun String.replaceNostrProfileUrisWithHandles(
    allUris: List<String>,
    referencedUsers: Set<ProfileDetailsUi>,
): String {
    var newContent = this
    referencedUsers.forEach { profileDetails ->
        val uri = allUris.find { it.contains(profileDetails.pubkey.hexToNpubHrp()) }
        if (uri != null) {
            newContent = newContent.replace(
                oldValue = uri,
                newValue = "@${profileDetails.userDisplayName}",
                ignoreCase = true,
            )
        }
    }
    return newContent
}
