package net.primal.android.user.services

import net.primal.android.networking.sockets.errors.WssException

interface ContactsService {
    /**
     * Fetches the latest contacts list from the cache server and
     * compares it with the version saved locally.
     *
     * @return A set of following pubkeys whose created_at date is latest
     */
    @Throws(WssException::class)
    suspend fun prepareContacts(): Set<String>
}