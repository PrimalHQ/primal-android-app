package net.primal.core.utils

import okio.ByteString.Companion.encodeUtf8

fun String.asSha256Hash() = encodeUtf8().sha256().hex()
