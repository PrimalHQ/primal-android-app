package net.primal.data.local.encryption


interface PlatformKeyStore {
    fun getOrCreateKey(): ByteArray
}

expect fun createPlatformKeyStore(): PlatformKeyStore
