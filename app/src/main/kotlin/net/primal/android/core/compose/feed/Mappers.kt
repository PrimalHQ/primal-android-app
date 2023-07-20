package net.primal.android.core.compose.feed

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.ProfileLinkUi
import net.primal.android.core.compose.media.model.MediaResourceUi
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.feed.db.FeedPost
import net.primal.android.nostr.ext.asEllipsizedNpub
import net.primal.android.nostr.ext.displayNameUiFriendly
import java.time.Instant

fun String.replaceNip21Links(): List<ProfileLinkUi> {
    val regex = Regex("(nostr:((npub|nprofile)[0-9a-z]+))")
    return regex.findAll(this).map { matchResult ->
        val npubOrNprofile = matchResult.groupValues[2]
        val link = matchResult.groupValues[1]
        ProfileLinkUi(npubOrNprofile, link, "TODO")
    }.toList()
}

//fun FeedPost.loadProfileLinks(profileRepository: ProfileRepository) : FeedPost {
//    val regex = Regex("(nostr:((npub|nprofile)[0-9a-z]+))")
//    this.profileLinks = regex.findAll(this.data.content).map { matchResult ->
//        val npubOrNprofile = matchResult.groupValues[2]
//        val link = matchResult.groupValues[1]
//        ProfileMetadata()
//    }.toList()
//    this
//}

fun FeedPost.asFeedPostUi(profiles: List<Profile>) = FeedPostUi(
    postId = this.data.postId,
    repostId = this.data.repostId,
    repostAuthorId = this.data.repostAuthorId,
    repostAuthorDisplayName = this.repostAuthor?.displayNameUiFriendly()
        ?: this.data.repostAuthorId?.asEllipsizedNpub(),
    authorId = this.author?.ownerId ?: this.data.authorId,
    authorDisplayName = this.author?.displayNameUiFriendly()
        ?: this.data.authorId.asEllipsizedNpub(),
    authorInternetIdentifier = this.author?.internetIdentifier,
    authorAvatarUrl = this.author?.picture,
    timestamp = Instant.ofEpochSecond(this.data.createdAt),
    content = this.data.content,
    authorResources = this.authorResources.map {
        MediaResourceUi(
            url = it.url,
            mimeType = it.contentType,
            variants = it.variants ?: emptyList(),
        )
    },
    postResources = this.postResources.map {
        MediaResourceUi(
            url = it.url,
            mimeType = it.contentType,
            variants = it.variants ?: emptyList(),
        )
    },
    profileLinks = this.data.content.replaceNip21Links(),
    stats = FeedPostStatsUi(
        repliesCount = this.postStats?.replies ?: 0,
        userReplied = this.userStats?.userReplied ?: false,
        zapsCount = this.postStats?.zaps ?: 0,
        satsZapped = this.postStats?.satsZapped ?: 0,
        userZapped = this.userStats?.userZapped ?: false,
        likesCount = this.postStats?.likes ?: 0,
        userLiked = this.userStats?.userLiked ?: false,
        repostsCount = this.postStats?.reposts ?: 0,
        userReposted = this.userStats?.userReposted ?: false,
    )
)
