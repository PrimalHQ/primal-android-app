@file:Suppress("SpellCheckingInspection")

package io.github.anvell.filetype

import io.github.anvell.filetype.Mime.Subtype
import io.github.anvell.filetype.Mime.Type
import io.github.anvell.filetype.matchers.CredentialsMatchers
import io.github.anvell.filetype.matchers.DocumentMatchers
import io.github.anvell.filetype.matchers.ImageMatchers
import io.github.anvell.filetype.matchers.VideoMatchers

object FileType {
    private val DefaultMatchers: Map<Mime, Matcher> = ImageMatchers +
        CredentialsMatchers +
        VideoMatchers +
        DocumentMatchers

    fun interface Matcher {
        operator fun invoke(buffer: ByteArray): Boolean
    }

    /**
     * Attempts to detect [Mime] from a given [buffer].
     */
    fun detect(buffer: ByteArray): Mime? {
        for ((fileType, matcher) in DefaultMatchers) {
            if (matcher(buffer)) return fileType
        }

        return null
    }

    /**
     * Detectable credentials [Mime].
     */
    object Credentials {
        val Jks = Mime(Type.Application, Subtype("x-java-keystore"))
        val Kdbx = Mime(Type.Application, Subtype("x-keepass"))
        val OpenSshPrivateKey = Mime(Type.Application, Subtype("openssh-private-key"))
        val Pem = Mime(Type.Application, Subtype("pem-certificate-chain"))
    }

    /**
     * Detectable document [Mime].
     */
    object Document {
        val Pdf = Mime(Type.Application, Subtype("pdf"))
        val Rtf = Mime(Type.Application, Subtype("rtf"))
    }

    /**
     * Detectable image [Mime].
     */
    object Image {
        val Avif = imageMime("avif")
        val Bmp = imageMime("bmp")
        val Dwg = imageMime("vnd.dwg")
        val Exr = imageMime("x-exr")
        val Gif = imageMime("gif")
        val Heif = imageMime("heif")
        val Ico = imageMime("vnd.microsoft.icon")
        val Jpeg = imageMime("jpeg")
        val Jpeg2000 = imageMime("jp2")
        val Jxr = imageMime("vnd.ms-photo")
        val Png = imageMime("png")
        val Psd = imageMime("vnd.adobe.photoshop")
        val Svg = imageMime("svg+xml")
        val Tiff = imageMime("tiff")
        val Webp = imageMime("webp")

        private fun imageMime(subtype: String) = Mime(Type.Image, Subtype(subtype))
    }

    object Video {
        val Avi = videoMime("avi")
        val Flv = videoMime("flv")
        val M4v = videoMime("m4v")
        val Mkv = videoMime("mkv")
        val Mov = videoMime("mov")
        val Mp4 = videoMime("mp4")
        val Mpeg = videoMime("mpeg")
        val Webm = videoMime("webm")
        val Wmv = videoMime("wmv")

        private fun videoMime(subtype: String) = Mime(Type.Video, Subtype(subtype))
    }
}
