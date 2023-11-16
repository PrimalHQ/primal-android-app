package net.primal.android.navigation

import androidx.navigation.NavOptionsBuilder
import java.net.URLDecoder
import java.net.URLEncoder
import org.apache.commons.lang3.CharEncoding

fun NavOptionsBuilder.clearBackStack() = popUpTo(id = 0)

fun String.asUrlEncoded(): String = URLEncoder.encode(this, CharEncoding.UTF_8)

fun String?.asUrlDecoded() =
    when (this) {
        null -> null
        else -> URLDecoder.decode(this, CharEncoding.UTF_8)
    }
