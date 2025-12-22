package net.primal.data.account.local.dao.apps.local

enum class LocalSignerMethodType {
    SignEvent,
    GetPublicKey,
    Nip04Encrypt,
    Nip04Decrypt,
    Nip44Encrypt,
    Nip44Decrypt,
    DecryptZapEvent,
}
