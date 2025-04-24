package net.primal.data.local.encryption

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object PlatformKeyStore {
    fun getOrCreateKey(): ByteArray
}
