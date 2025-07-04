package net.primal.android.core.images

import android.content.Context
import android.os.Build
import coil3.ImageLoader
import coil3.decode.BitmapFactoryDecoder
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import okio.Path.Companion.toOkioPath

object AvatarCoilImageLoader {
    private var defaultImageLoader: ImageLoader? = null
    private var noGifsImageLoader: ImageLoader? = null

    fun provideImageLoader(context: Context): ImageLoader =
        defaultImageLoader ?: constructImageLoader(context = context).also { defaultImageLoader = it }

    fun provideNoGifsImageLoader(context: Context): ImageLoader =
        noGifsImageLoader ?: constructNoGifsImageLoader(context = context).also { noGifsImageLoader = it }

    private fun constructNoGifsImageLoader(context: Context): ImageLoader =
        getSharedImageLoaderBuilder(context = context)
            .components {
                add(BitmapFactoryDecoder.Factory())
            }
            .build()

    private fun constructImageLoader(context: Context): ImageLoader =
        getSharedImageLoaderBuilder(context = context)
            .components {
                // Gifs
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()

    private fun getSharedImageLoaderBuilder(context: Context): ImageLoader.Builder =
        ImageLoader.Builder(context = context)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("avatar_image_cache").toOkioPath())
                    .build()
            }
}
