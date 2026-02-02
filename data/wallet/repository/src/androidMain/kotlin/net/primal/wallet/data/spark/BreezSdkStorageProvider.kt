package net.primal.wallet.data.spark

import android.content.Context
import java.io.File

/**
 * Android implementation using app's internal storage sandbox.
 */
internal object AndroidBreezSdkStorageProvider : BreezSdkStorageProvider {

    private const val BREEZ_SDK_DIR = "breez_sdk"

    private var filesDir: File? = null

    fun init(context: Context) {
        filesDir = context.applicationContext.filesDir
    }

    private fun getBaseDir(): File {
        val baseDir = filesDir ?: error("AndroidBreezSdkStorageProvider not initialized. Call init(context) first.")
        return File(baseDir, BREEZ_SDK_DIR)
    }

    override fun getStorageDirectory(walletId: String): String {
        val dir = File(getBaseDir(), walletId)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath
    }

    override fun deleteStorage(walletId: String) {
        val dir = File(getBaseDir(), walletId)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }
}

internal actual fun createBreezSdkStorageProvider(): BreezSdkStorageProvider = AndroidBreezSdkStorageProvider
