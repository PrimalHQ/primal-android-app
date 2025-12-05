package net.primal.android.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.core.images.AvatarCoilImageLoader
import net.primal.android.core.images.CoilMediaCacher
import net.primal.core.caching.MediaCacher

@Module
@InstallIn(SingletonComponent::class)
object ImagesModule {

    @Provides
    @Singleton
    fun provideMediaCacher(@ApplicationContext context: Context): MediaCacher {
        val avatarImageLoader = AvatarCoilImageLoader.provideImageLoader(context)
        return CoilMediaCacher(context, avatarImageLoader)
    }
}
