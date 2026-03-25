package net.primal.android.namecoin.electrumx

/**
 * Typed exceptions for Namecoin name resolution failures.
 */
sealed class NamecoinLookupException(message: String) : Exception(message) {
    /** Name does not exist on the Namecoin blockchain. */
    class NameNotFound(val identifier: String) : NamecoinLookupException("Name not found: $identifier")

    /** Name exists but has expired (>36,000 blocks since last update). */
    class NameExpired(val identifier: String, val blocksSinceUpdate: Int? = null) :
        NamecoinLookupException("Name expired: $identifier")

    /** All configured ElectrumX servers failed to respond. */
    class ServersUnreachable(message: String = "All ElectrumX servers unreachable") :
        NamecoinLookupException(message)

    /** Name exists on-chain but contains no valid Nostr pubkey. */
    class NoNostrKey(val identifier: String) :
        NamecoinLookupException("No Nostr key found in name value: $identifier")

    /** Server returned a malformed or unparseable response. */
    class ParseError(val identifier: String, cause: Throwable? = null) :
        NamecoinLookupException("Failed to parse response for: $identifier")
}
