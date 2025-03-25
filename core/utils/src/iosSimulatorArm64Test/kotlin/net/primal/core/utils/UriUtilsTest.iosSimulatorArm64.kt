package net.primal.core.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

@OptIn(ExperimentalForeignApi::class)
actual fun getResourceContent(path: String): String? {
    val bundle = NSBundle.mainBundle
    val path = bundle.pathForResource(path, "txt")
        ?: throw IllegalArgumentException("Resource not found: $path")
    return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as String
}
