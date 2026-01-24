package net.primal.wallet.data.spark

import breez_sdk_spark.BreezSdk
import breez_sdk_spark.ConnectWithSignerRequest
import breez_sdk_spark.KeySetConfig
import breez_sdk_spark.KeySetType
import breez_sdk_spark.MaxFee
import breez_sdk_spark.Network
import breez_sdk_spark.SignMessageRequest
import breez_sdk_spark.connectWithSigner
import breez_sdk_spark.defaultConfig
import breez_sdk_spark.defaultExternalSigner
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages Breez SDK instances for multiple wallets.
 *
 * Each wallet (identified by walletId) has its own SDK instance.
 * Thread-safe: Uses mutex to protect concurrent access.
 */
internal class BreezSdkInstanceManager(
    private val storageProvider: BreezSdkStorageProvider,
    private val apiKey: String,
) {

    init {
        BreezSdkNapierLogger.ensureInitialized()
    }

    private val mutex = Mutex()
    private val instances = mutableMapOf<String, BreezSdk>()

    /**
     * Creates and initializes a new Spark wallet from seed words.
     *
     * This method:
     * 1. Connects with temporary storage to derive the walletId (pubkey)
     * 2. Cleans up temporary storage
     * 3. Reconnects with proper walletId-based storage
     *
     * After creation, use [getInstance] or [requireInstance] to access the SDK.
     *
     * @param seedWords BIP39 mnemonic seed words (12 or 24 words)
     * @return The walletId (pubkey) for the created wallet
     * @throws IllegalStateException if wallet already exists
     */
    suspend fun createWallet(seedWords: String): String =
        mutex.withLock {
            // Connect with temp storage to derive walletId
            val tempId = "temp_init"
            val tempSdk = connectSdk(tempId, seedWords)

            // Get the pubkey by signing a message - this gives us the wallet's identity
            // Wrap in try-finally to ensure cleanup even if signMessage fails
            val walletId = try {
                val signResponse = tempSdk.signMessage(
                    SignMessageRequest(message = "primal_wallet_init", compact = true),
                )
                signResponse.pubkey
            } finally {
                // Always cleanup temp instance
                tempSdk.disconnect()
                tempSdk.close()
                storageProvider.deleteStorage(tempId)
            }

            // Check if wallet already exists in memory
            if (instances.containsKey(walletId)) {
                throw IllegalStateException("Wallet already exists: $walletId")
            }

            // Reconnect with proper walletId-based storage
            val sdk = connectSdk(walletId, seedWords)
            instances[walletId] = sdk

            return walletId
        }

    private suspend fun connectSdk(walletId: String, seedWords: String): BreezSdk {
        val config = defaultConfig(Network.MAINNET).apply {
            apiKey = this@BreezSdkInstanceManager.apiKey
            maxDepositClaimFee = MaxFee.Fixed(amount = 50_000.toULong())
            syncIntervalSecs = 15.toUInt()
            privateEnabledDefault = true
            preferSparkOverLightning = true
        }

        val storageDir = storageProvider.getStorageDirectory(walletId)

        val signer = defaultExternalSigner(
            mnemonic = seedWords,
            passphrase = null,
            network = Network.MAINNET,
            keySetConfig = KeySetConfig(
                keySetType = KeySetType.DEFAULT,
                useAddressIndex = false,
                accountNumber = 0u,
            ),
        )

        return connectWithSigner(
            request = ConnectWithSignerRequest(
                config = config,
                signer = signer,
                storageDir = storageDir,
            ),
        )
    }

    /**
     * Gets an existing SDK instance.
     *
     * @param walletId Unique identifier for the wallet
     * @return The SDK instance, or null if not found
     */
    suspend fun getInstance(walletId: String): BreezSdk? =
        mutex.withLock {
            instances[walletId]
        }

    /**
     * Gets an existing SDK instance or throws.
     *
     * @param walletId Unique identifier for the wallet
     * @return The SDK instance
     * @throws IllegalStateException if wallet not found
     */
    suspend fun requireInstance(walletId: String): BreezSdk =
        mutex.withLock {
            instances[walletId] ?: throw IllegalStateException("Wallet not found: $walletId")
        }

    /**
     * Disconnects and removes an SDK instance.
     *
     * @param walletId Unique identifier for the wallet
     * @return true if wallet was found and removed, false otherwise
     */
    suspend fun removeInstance(walletId: String): Boolean =
        mutex.withLock {
            val instance = instances.remove(walletId)
            if (instance != null) {
                instance.disconnect()
                instance.close()
                true
            } else {
                false
            }
        }

    /**
     * Checks if an instance exists for the given walletId.
     */
    suspend fun hasInstance(walletId: String): Boolean =
        mutex.withLock {
            walletId in instances
        }
}
