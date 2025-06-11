package net.primal.data.local.encryption

object JvmPlatformKeyStore : PlatformKeyStore {
    override fun getOrCreateKey(): ByteArray = throw NotImplementedError()
}

actual fun createPlatformKeyStore(): PlatformKeyStore = JvmPlatformKeyStore
