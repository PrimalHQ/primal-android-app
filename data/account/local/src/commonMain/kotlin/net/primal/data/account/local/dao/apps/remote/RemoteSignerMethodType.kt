package net.primal.data.account.local.dao.apps.remote

enum class RemoteSignerMethodType {
    Connect,
    Ping,
    SignEvent,
    GetPublicKey,
    Nip04Encrypt,
    Nip04Decrypt,
    Nip44Encrypt,
    Nip44Decrypt,
}
