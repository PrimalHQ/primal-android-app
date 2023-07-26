package net.primal.android.navigation

import androidx.lifecycle.SavedStateHandle


const val FeedDirective = "directive"

inline val SavedStateHandle.feedDirective: String? get() = get<String>(FeedDirective)?.asUrlDecoded()


const val PostId = "postId"

inline val SavedStateHandle.postId: String
    get() = get(PostId) ?: throw IllegalArgumentException("Missing required postId argument.")


const val ProfileId = "profileId"

inline val SavedStateHandle.profileId: String? get() = get(ProfileId)


const val SearchQuery = "searchQuery"

inline val SavedStateHandle.searchQuery: String
    get() = get(SearchQuery) ?: throw IllegalArgumentException("Missing required searchQuery argument.")


const val NewPostPreFillContent = "preFillContent"

inline val SavedStateHandle.newPostPreFillContent: String? get() = get<String>(NewPostPreFillContent)?.asUrlDecoded()
