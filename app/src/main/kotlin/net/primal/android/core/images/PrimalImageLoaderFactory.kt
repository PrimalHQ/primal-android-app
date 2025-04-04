package net.primal.android.core.images

import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.video.VideoFrameDecoder
import javax.inject.Inject
import javax.inject.Singleton
import okio.Path.Companion.toOkioPath

@Singleton
class PrimalImageLoaderFactory @Inject constructor() : SingletonImageLoader.Factory {

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val defaultBuilder = ImageLoader.Builder(context)
        val imageCacheDir = context.cacheDir.resolve("image_cache").toOkioPath()

        return defaultBuilder
            .components {
                // Gifs
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }

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
