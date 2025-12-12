package net.primal.android.core.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import net.primal.core.caching.MediaCacher

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class FeedVideoCache

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class StreamVideoCache

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SimpleCacheFactoryProvider {
    @UnstableApi
    @FeedVideoCache
    fun feedVideoCache(): SimpleCache

    @UnstableApi
    @StreamVideoCache
    fun streamVideoCache(): SimpleCache
}

@UnstableApi
@Composable
fun rememberFeedVideoCache(): SimpleCache {
    val context = LocalContext.current
    return remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SimpleCacheFactoryProvider::class.java,
        ).feedVideoCache()
    }
}

@UnstableApi
@Composable
fun rememberStreamVideoCache(): SimpleCache {
    val context = LocalContext.current
    return remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SimpleCacheFactoryProvider::class.java,
        ).streamVideoCache()
    }
}

@Composable
fun rememberMediaCacher(): MediaCacher {
    val context = LocalContext.current
    return remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ImagesModule.ImagesEntryPoint::class.java,
        ).mediaCacher()
    }
}
