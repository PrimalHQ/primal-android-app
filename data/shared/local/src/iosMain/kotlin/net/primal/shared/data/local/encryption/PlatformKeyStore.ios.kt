package net.primal.shared.data.local.encryption

import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.providers.base.toByteArray
import dev.whyoleg.cryptography.providers.base.toNSData
import kotlin.random.Random
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFAutorelease
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class, CryptographyProviderApi::class)
object IosPlatformKeyStore : PlatformKeyStore {

    private const val SERVICE_NAME = "primal.keystore"
    private const val ACCOUNT_NAME = "encryption_key_v1"
    private const val KEY_SIZE_BYTES = 32

    override fun getOrCreateKey(): ByteArray {
        getKeyFromKeychain()?.let { return it }
        val newKey = generateRandomKey()
        saveKeyToKeychain(key = newKey)
        return newKey
    }

    private fun generateRandomKey(): ByteArray {
        return ByteArray(KEY_SIZE_BYTES).also { Random.Default.nextBytes(it) }
    }

    private fun getKeyFromKeychain(): ByteArray? =
        memScoped {
            val query = buildReadQuery() ?: return null
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            if (status == errSecSuccess && result.value != null) {
                val data = CFBridgingRelease(result.value) as? NSData
                data?.toByteArray()
            } else {
                null
            }
        }

    private fun saveKeyToKeychain(key: ByteArray): Boolean =
        memScoped {
            deleteKeyFromKeychain()
            val keyData = key.toNSData()
            val query = buildWriteQuery(keyData = keyData) ?: return false
            SecItemAdd(query, null) == errSecSuccess
        }

    private fun deleteKeyFromKeychain(): Unit =
        memScoped {
            val query = buildDeleteQuery() ?: return
            SecItemDelete(query)
        }

    private fun buildReadQuery() =
        CFDictionaryCreateMutable(null, 4, null, null)?.apply {
            setBaseAttributes()
            CFDictionarySetValue(this, kSecReturnData, kCFBooleanTrue)
            CFAutorelease(this)
        }

    private fun buildWriteQuery(keyData: NSData) =
        CFDictionaryCreateMutable(null, 5, null, null)?.apply {
            setBaseAttributes()
            CFDictionarySetValue(this, kSecValueData, CFBridgingRetain(keyData))
            CFDictionarySetValue(
                this,
                kSecAttrAccessible,
                kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
            )
            CFAutorelease(this)
        }

    private fun buildDeleteQuery() =
        CFDictionaryCreateMutable(null, 3, null, null)?.apply {
            setBaseAttributes()
            CFAutorelease(this)
        }

    private fun CFMutableDictionaryRef.setBaseAttributes() {
        CFDictionarySetValue(this, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(this, kSecAttrService, CFBridgingRetain(SERVICE_NAME))
        CFDictionarySetValue(this, kSecAttrAccount, CFBridgingRetain(ACCOUNT_NAME))
    }
}

actual fun createPlatformKeyStore(): PlatformKeyStore = IosPlatformKeyStore
