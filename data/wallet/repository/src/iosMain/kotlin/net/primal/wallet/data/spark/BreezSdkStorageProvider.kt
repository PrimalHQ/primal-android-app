package net.primal.wallet.data.spark

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation using NSDocumentDirectory.
 */
internal object IosBreezSdkStorageProvider : BreezSdkStorageProvider {

    private const val BREEZ_SDK_DIR = "breez_sdk"

    @OptIn(ExperimentalForeignApi::class)
    override fun getStorageDirectory(walletId: String): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val basePath = requireNotNull(documentDirectory?.path) {
            "Failed to get iOS document directory"
        }
        val path = "$basePath/$BREEZ_SDK_DIR/$walletId"

        // Create directory if it doesn't exist
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(path)) {
            fileManager.createDirectoryAtPath(
                path = path,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }

        return path
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun deleteStorage(walletId: String) {
        val path = getStorageDirectory(walletId)
        NSFileManager.defaultManager.removeItemAtPath(path, error = null)
    }
}

internal actual fun createBreezSdkStorageProvider(): BreezSdkStorageProvider = IosBreezSdkStorageProvider
