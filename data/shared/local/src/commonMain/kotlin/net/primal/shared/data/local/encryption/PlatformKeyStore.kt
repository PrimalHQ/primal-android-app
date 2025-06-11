package net.primal.shared.data.local.encryption

interface PlatformKeyStore {
    fun getOrCreateKey(): ByteArray
}

expect fun createPlatformKeyStore(): PlatformKeyStore
