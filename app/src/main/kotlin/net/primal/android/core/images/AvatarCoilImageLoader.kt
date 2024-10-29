package net.primal.android.core.images

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache

object AvatarCoilImageLoader {
    private var avatarCoilImageLoader: ImageLoader? = null
    private var noGifsImageLoader: ImageLoader? = null

    fun provideImageLoader(context: Context): ImageLoader =
        avatarCoilImageLoader ?: constructImageLoader(context = context).also { avatarCoilImageLoader = it }

    fun provideNoGifsImageLoader(context: Context): ImageLoader =
        noGifsImageLoader ?: constructNoGifsImageLoader(context = context).also { noGifsImageLoader = it }

    private fun constructNoGifsImageLoader(context: Context): ImageLoader =
        getSharedImageLoaderBuilder(context = context).build()

    private fun constructImageLoader(context: Context): ImageLoader =
        getSharedImageLoaderBuilder(context = context)
            .components {
                // Gifs
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }

                // Video frames
                add(VideoFrameDecoder.Factory())
            }
            .build()

    private fun getSharedImageLoaderBuilder(context: Context): ImageLoader.Builder =
        ImageLoader.Builder(context = context)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("avatar_image_cache"))
                    .build()
            }
}
