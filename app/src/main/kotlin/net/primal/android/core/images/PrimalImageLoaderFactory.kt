package net.primal.android.core.images

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.gif.GifDecoder
import coil3.video.VideoFrameDecoder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import okio.Path
import okio.Path.Companion.toOkioPath

@Singleton
class PrimalImageLoaderFactory @Inject constructor(
    @ApplicationContext private val context: PlatformContext,
) : SingletonImageLoader.Factory {

    private val defaultBuilder by lazy { ImageLoader.Builder(context) }
    private val imageCacheDir: Path by lazy { context.cacheDir.resolve("image_cache").toOkioPath() }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return defaultBuilder
            .components {
                // Gifs
                add(GifDecoder.Factory())

                // Video frames
                add(VideoFrameDecoder.Factory())
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(imageCacheDir)
                    .maxSizePercent(percent = 0.02)
                    .build()
            }
            .build()
    }
}
