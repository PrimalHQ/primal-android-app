package net.primal.android.core.images

import android.content.Context
import coil.ImageLoader

object NoGifsCoilImageLoader {

    private var noGifsImageLoader: ImageLoader? = null

    fun noGifsImageLoader(context: Context): ImageLoader {
        return noGifsImageLoader ?: ImageLoader.Builder(context).build()
    }
}
