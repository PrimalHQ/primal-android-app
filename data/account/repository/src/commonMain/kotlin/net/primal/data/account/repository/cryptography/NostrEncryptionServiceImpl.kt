package net.primal.data.account.repository.cryptography

import com.vitorpamplona.quartz.nip04Dm.crypto.Nip04
import com.vitorpamplona.quartz.nip44Encryption.Nip44v2
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.domain.account.cryptography.NostrEncryptionService
import net.primal.domain.nostr.cryptography.utils.bechToBytesOrThrow
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.nostr.cryptography.utils.hexToNsecHrp

class NostrEncryptionServiceImpl : NostrEncryptionService {
    val nip44 = Nip44v2()

    override fun nip04Encrypt(
        privateKey: String,
        pubKey: String,
        plaintext: String,
    ): Result<String> =
        runCatching {
            Nip04.encrypt(
                privateKey = privateKey.hexToNsecHrp().bechToBytesOrThrow(),
                pubKey = pubKey.hexToNpubHrp().bechToBytesOrThrow(),
                msg = plaintext,
            )
        }

    override fun nip04Decrypt(
        privateKey: String,
        pubKey: String,
        ciphertext: String,
    ): Result<String> =
        runCatching {
            Nip04.decrypt(
                privateKey = privateKey.hexToNsecHrp().bechToBytesOrThrow(),
                pubKey = pubKey.hexToNpubHrp().bechToBytesOrThrow(),
                msg = ciphertext,
            )
        }

    override fun nip44Encrypt(
        privateKey: String,
        pubKey: String,
        plaintext: String,
    ): Result<String> =
        runCatching {
            nip44.encrypt(
                privateKey = privateKey.hexToNsecHrp().bechToBytesOrThrow(),
                pubKey = pubKey.hexToNpubHrp().bechToBytesOrThrow(),
                msg = plaintext,
            ).encodePayload()
        }

    override fun nip44Decrypt(
        privateKey: String,
        pubKey: String,
        ciphertext: String,
    ): Result<String> =
        runCatching {
            nip44.decrypt(
                pubKey = pubKey.hexToNpubHrp().bechToBytesOrThrow(),
                privateKey = privateKey.hexToNsecHrp().bechToBytesOrThrow(),
                payload = ciphertext,
            )
        }
}
