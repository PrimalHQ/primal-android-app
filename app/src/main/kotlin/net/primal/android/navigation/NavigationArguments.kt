package net.primal.android.navigation

import androidx.lifecycle.SavedStateHandle


const val FeedDirective = "directive"
inline val SavedStateHandle.feedDirective: String? get() = get<String>(FeedDirective)?.asUrlDecoded()


const val PostId = "postId"
inline val SavedStateHandle.postIdOrThrow: String
    get() = get(PostId) ?: throw IllegalArgumentException("Missing required postId argument.")


const val ProfileId = "profileId"
inline val SavedStateHandle.profileId: String? get() = get(ProfileId)
inline val SavedStateHandle.profileIdOrThrow: String
    get() = get(ProfileId) ?: throw IllegalArgumentException("Missing required profileId argument")


const val SearchQuery = "searchQuery"
inline val SavedStateHandle.searchQueryOrThrow: String
    get() = get(SearchQuery) ?: throw IllegalArgumentException("Missing required searchQuery argument.")


const val NewPostPreFillContent = "preFillContent"
inline val SavedStateHandle.newPostPreFillContent: String? get() = get<String>(NewPostPreFillContent)?.asUrlDecoded()?.ifEmpty { null }


const val NewPostPreFillFileUri = "preFillFileUri"
inline val SavedStateHandle.newPostPreFillFileUri: String? get() = get<String?>(NewPostPreFillFileUri)?.asUrlDecoded()?.ifEmpty { null }


const val NewPostReplyToNoteId = "replyToNoteId"
inline val SavedStateHandle.replyToNoteId: String? get() = get<String?>(NewPostReplyToNoteId)?.ifEmpty { null }


const val NWCUrl = "nwcUrl"
inline val SavedStateHandle.nwcUrl: String? get() = get(NWCUrl)
