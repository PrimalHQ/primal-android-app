package net.primal.android.gifpicker.domain

import net.primal.data.remote.api.klipy.model.KlipyGif

data class GifItem(
    val id: String,
    val url: String,
    val previewUrl: String,
    val previewWidth: Int = 0,
    val previewHeight: Int = 0,
    val contentDescription: String = "",
)

fun KlipyGif.asGifItem(): GifItem? {
    val tinyGif = mediaFormats["tinygif"]
    val fullGif = mediaFormats["gif"]
    if (tinyGif == null || fullGif == null) return null
    return GifItem(
        id = id,
        url = fullGif.url,
        previewUrl = tinyGif.url,
        previewWidth = tinyGif.dims.getOrElse(0) { 0 },
        previewHeight = tinyGif.dims.getOrElse(1) { 0 },
        contentDescription = contentDescription,
    )
}
