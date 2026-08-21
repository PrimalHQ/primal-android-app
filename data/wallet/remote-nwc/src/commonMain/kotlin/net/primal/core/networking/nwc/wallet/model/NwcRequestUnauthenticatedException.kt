package net.primal.core.networking.nwc.wallet.model

/**
 * Raised when an incoming NIP-47 request is not provably from the connection's authorized client.
 *
 * Callers must drop these silently. Responding would publish a relay event addressed to the
 * legitimate client on behalf of an unauthenticated sender, and would confirm to a prober that
 * their event reached a live wallet service.
 */
class NwcRequestUnauthenticatedException(val eventId: String) :
    Exception("NWC request $eventId failed authentication.")
