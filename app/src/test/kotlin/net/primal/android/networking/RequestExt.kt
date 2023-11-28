package net.primal.android.networking

import okhttp3.Request

fun Request.asWssUrl() = url.toString().replace("https", "wss")
