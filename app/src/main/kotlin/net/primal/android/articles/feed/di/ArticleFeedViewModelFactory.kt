package net.primal.android.articles.feed.di

import dagger.assisted.AssistedFactory
import net.primal.android.articles.feed.ArticleFeedViewModel

@AssistedFactory
interface ArticleFeedViewModelFactory {
    fun create(spec: String): ArticleFeedViewModel
}
