package net.primal.shared.data.local.encryption

object IosPlatformKeyStore : PlatformKeyStore {
    override fun getOrCreateKey(): ByteArray = throw NotImplementedError()
}

actual fun createPlatformKeyStore(): PlatformKeyStore = IosPlatformKeyStore
