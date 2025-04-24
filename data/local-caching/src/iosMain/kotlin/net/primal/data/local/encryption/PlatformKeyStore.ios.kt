package net.primal.data.local.encryption

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object PlatformKeyStore {
    actual fun getOrCreateKey(): ByteArray = throw NotImplementedError()
}
