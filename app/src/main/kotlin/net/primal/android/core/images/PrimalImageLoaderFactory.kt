package net.primal.android.core.images

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrimalImageLoaderFactory @Inject constructor(
    @ApplicationContext context: Context,
) : ImageLoaderFactory {

    private val defaultBuilder by lazy { ImageLoader.Builder(context) }

    override fun newImageLoader(): ImageLoader {
        return defaultBuilder
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}
