package net.primal.android.navigation

import androidx.lifecycle.SavedStateHandle


const val FeedDirective = "directive"

inline val SavedStateHandle.feedDirective: String? get() = get<String>(FeedDirective)?.asUrlDecoded()


const val PostId = "postId"

inline val SavedStateHandle.postId: String
    get() = get(PostId) ?: throw IllegalArgumentException("Missing required postId argument.")

