package net.primal.core.utils

actual fun getResourceContent(path: String): String? {
    val resource = object {}.javaClass.getResource(path).readText()
    return resource}
