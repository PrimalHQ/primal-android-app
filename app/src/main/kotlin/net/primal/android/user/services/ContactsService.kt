package net.primal.android.user.services

import net.primal.android.networking.primal.PrimalApiException
import net.primal.android.networking.primal.PrimalQueryResult
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.domain.UserAccount

interface ContactsService {
    /**
     * Compares local contacts list and the one returned from cache server and
     * returns the latest valid one
     *
     * @return A set of following pubkeys
     */
    @Throws(WssException::class, PrimalApiException::class)
    fun prepareContacts(activeUserAccount: UserAccount, queryResult: PrimalQueryResult): Set<String>
}