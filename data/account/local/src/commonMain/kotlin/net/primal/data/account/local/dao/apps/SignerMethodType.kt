package net.primal.data.account.local.dao.apps

enum class SignerMethodType {
    Connect,
    Ping,
    SignEvent,
    GetPublicKey,
    Nip04Encrypt,
    Nip04Decrypt,
    Nip44Encrypt,
    Nip44Decrypt,
    DecryptZapEvent,
}
