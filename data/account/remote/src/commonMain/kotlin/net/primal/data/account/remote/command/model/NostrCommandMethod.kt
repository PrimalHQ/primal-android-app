package net.primal.data.account.remote.command.model

enum class NostrCommandMethod {
    Connect,
    Ping,
    SignEvent,
    GetPublicKey,
    Nip04Encrypt,
    Nip04Decrypt,
    Nip44Encrypt,
    Nip44Decrypt,
}
