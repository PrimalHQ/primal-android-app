package net.primal.android.core.di

import android.content.Context
import coil3.SingletonImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.core.images.AvatarCoilImageLoader
import net.primal.android.core.images.CoilMediaCacher
import net.primal.core.caching.MediaCacher
import net.primal.core.utils.coroutines.DispatcherProvider

@Module
@InstallIn(SingletonComponent::class)
object ImagesModule {

    @Provides
    @Singleton
    fun provideMediaCacher(@ApplicationContext context: Context, dispatchers: DispatcherProvider): MediaCacher {
        val avatarImageLoader = AvatarCoilImageLoader.provideImageLoader(context)
        val feedImageLoader = SingletonImageLoader.get(context)

        return CoilMediaCacher(
            context = context,
            dispatchers = dispatchers,
            avatarImageLoader = avatarImageLoader,
            feedImageLoader = feedImageLoader,
        )
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ImagesEntryPoint {
        fun mediaCacher(): MediaCacher
    }
}
