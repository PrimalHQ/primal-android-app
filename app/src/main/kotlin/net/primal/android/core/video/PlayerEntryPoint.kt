package net.primal.android.core.video

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.core.di.FeedVideoCache

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PlayerEntryPoint {
    @OptIn(UnstableApi::class)
    @FeedVideoCache
    fun simpleCache(): SimpleCache
}
