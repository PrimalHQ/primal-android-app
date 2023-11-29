package net.primal.android.navigation

import androidx.navigation.NavOptionsBuilder
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*
import org.apache.commons.lang3.CharEncoding

fun NavOptionsBuilder.clearBackStack() = popUpTo(id = 0)

fun String.asUrlEncoded(): String = URLEncoder.encode(this, CharEncoding.UTF_8)

fun String?.asUrlDecoded() =
    when (this) {
        null -> null
        else -> URLDecoder.decode(this, CharEncoding.UTF_8)
    }

fun String.asBase64Encoded() = String(Base64.getEncoder().encode(this.toByteArray()))

fun String?.asBase64Decoded(): String? {
    return when (this) {
        null -> null
        else -> String(Base64.getDecoder().decode(this.toByteArray()))
    }
}
