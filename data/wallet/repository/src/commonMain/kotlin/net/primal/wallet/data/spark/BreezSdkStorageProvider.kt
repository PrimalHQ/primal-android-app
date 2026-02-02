package net.primal.wallet.data.spark

/**
 * Provides platform-specific storage directory for Spark wallet data.
 */
internal interface BreezSdkStorageProvider {
    /**
     * Returns the storage directory path for a specific wallet.
     * The directory will be created if it doesn't exist.
     *
     * @param walletId Unique identifier for the wallet
     * @return Absolute or relative path to the wallet's storage directory
     */
    fun getStorageDirectory(walletId: String): String

    /**
     * Deletes the storage directory for a specific wallet.
     * Used to clean up temporary storage after wallet initialization.
     *
     * @param walletId Unique identifier for the wallet
     */
    fun deleteStorage(walletId: String)
}

/**
 * Creates a platform-specific BreezSdkStorageProvider instance.
 */
internal expect fun createBreezSdkStorageProvider(): BreezSdkStorageProvider
