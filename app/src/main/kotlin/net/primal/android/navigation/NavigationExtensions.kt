package net.primal.android.navigation

import androidx.navigation.NavOptionsBuilder
import org.apache.commons.lang3.CharEncoding
import java.net.URLDecoder
import java.net.URLEncoder

fun NavOptionsBuilder.clearBackStack() = popUpTo(id = 0)

fun String.asUrlEncoded(): String = URLEncoder.encode(this, CharEncoding.UTF_8)

fun String?.asUrlDecoded() = when (this) {
    null -> null
    else -> URLDecoder.decode(this, CharEncoding.UTF_8)
}

