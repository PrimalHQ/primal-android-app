package net.primal.android.user.impl

import net.primal.android.networking.primal.PrimalApiException
import net.primal.android.networking.primal.PrimalQueryResult
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.user.accounts.parseFollowings
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.services.ContactsService
import javax.inject.Inject

class ContactsServiceImpl @Inject constructor(): ContactsService {
    override fun prepareContacts(activeUserAccount: UserAccount, queryResult: PrimalQueryResult): Set<String> {
        val contactsEvent = queryResult.findNostrEvent(NostrEventKind.Contacts)
            ?: throw PrimalApiException.ContactListNotFound

        val contactsTags = contactsEvent.tags
            ?: throw PrimalApiException.ContactListTagsNotFound

        val followings = contactsTags.parseFollowings()

        val result = when {
            activeUserAccount.contactsCreatedAt == null -> throw PrimalApiException.ContactListCreatedAtNotFound
            activeUserAccount.contactsCreatedAt > contactsEvent.createdAt -> activeUserAccount.following
            contactsEvent.createdAt >= activeUserAccount.contactsCreatedAt -> followings
            else -> setOf()
        }

        return result
    }
}