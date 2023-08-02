package net.primal.android.user.impl

import kotlinx.serialization.encodeToString
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.Verb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.serialization.NostrJson
import net.primal.android.user.active.ActiveAccountStore
import net.primal.android.user.api.model.UserRequestBody
import net.primal.android.user.services.ContactsService
import javax.inject.Inject

class ContactsServiceImpl @Inject constructor(
    private val aas: ActiveAccountStore,
    private val pac: PrimalApiClient
): ContactsService {
    override suspend fun prepareContacts(): Set<String> {
        val activeUserAccount = aas.activeUserAccount.value

        val queryResult = pac.query(
            message = PrimalCacheFilter(
                primalVerb = Verb.CONTACT_LIST,
                optionsJson = NostrJson.encodeToString(UserRequestBody(pubkey = aas.activeUserId(), extendedResponse = false))
            )
        )

        val contactsEvent = queryResult.findNostrEvent(NostrEventKind.Contacts)

        //TODO: Multiple checks between existing contacts list and contacts list we get from the cache server

        return mutableSetOf()
    }
}