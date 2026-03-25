/*
 * ViewModel for follow list import during signup.
 *
 * Ported from Amethyst PR #1785 by mstrofnone, adapted for Primal's Hilt DI
 * and caching API.
 *
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.auth.onboarding.account.followimport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.auth.onboarding.account.followimport.ImportFollowListContract.Phase
import net.primal.android.auth.onboarding.account.followimport.ImportFollowListContract.UiEvent
import net.primal.android.auth.onboarding.account.followimport.ImportFollowListContract.UiState
import net.primal.android.namecoin.NamecoinNameService
import net.primal.android.namecoin.electrumx.NamecoinLookupException
import net.primal.android.namecoin.electrumx.NamecoinNameResolver
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.PrimalVerb
import net.primal.data.remote.api.users.model.FollowListRequestBody
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind

private val HEX_PUBKEY_REGEX = Regex("^[0-9a-fA-F]{64}$")

@HiltViewModel
class ImportFollowListViewModel @Inject constructor(
    private val namecoinNameService: NamecoinNameService,
    @PrimalCacheApiClient private val primalApiClient: PrimalApiClient,
) : ViewModel() {

    private val importer = FollowListImporter(
        resolveNamecoin = { identifier ->
            try {
                namecoinNameService.resolve(identifier).pubkey
            } catch (_: NamecoinLookupException) {
                null
            }
        },
    )

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() = viewModelScope.launch {
        events.collect { event ->
            when (event) {
                is UiEvent.IdentifierChanged -> setState { copy(identifier = event.identifier) }
                is UiEvent.StartImport -> startImport()
                is UiEvent.ToggleSelection -> toggleSelection(event.pubkeyHex)
                is UiEvent.SetSelectAll -> setSelectAll(event.selectAll)
                is UiEvent.ApplyFollows -> { /* handled externally via onApplyFollows callback */ }
                is UiEvent.Reset -> reset()
            }
        }
    }

    private fun startImport() {
        val identifier = _state.value.identifier.trim()
        if (identifier.isBlank()) {
            setState { copy(phase = Phase.Error, errorMessage = "Please enter an identifier.") }
            return
        }

        viewModelScope.launch {
            setState { copy(phase = Phase.Resolving) }

            try {
                // Resolve identifier to pubkey
                val resolved = resolveWithDetailedErrors(identifier)
                if (resolved == null) {
                    setState {
                        copy(
                            phase = Phase.Error,
                            errorMessage = "Could not resolve \"$identifier\" to a public key. " +
                                "Enter an npub, hex pubkey, NIP-05, or Namecoin name (.bit / d/ / id/).",
                        )
                    }
                    return@launch
                }

                // Fetch contact list via Primal caching API
                setState { copy(phase = Phase.Fetching) }
                val follows = fetchFollowList(resolved.pubkeyHex)

                if (follows == null) {
                    setState {
                        copy(phase = Phase.Error, errorMessage = "No follow list found for this user.")
                    }
                    return@launch
                }

                if (follows.isEmpty()) {
                    setState {
                        copy(phase = Phase.Error, errorMessage = "This user's follow list is empty.")
                    }
                    return@launch
                }

                setState {
                    copy(
                        phase = Phase.Preview,
                        sourcePubkeyHex = resolved.pubkeyHex,
                        follows = follows,
                        selected = follows.map { it.pubkeyHex }.toSet(),
                        namecoinSource = resolved.namecoinSource,
                    )
                }
            } catch (e: Exception) {
                Napier.w(throwable = e) { "Failed to import follow list" }
                setState {
                    copy(phase = Phase.Error, errorMessage = e.message ?: "Unknown error occurred.")
                }
            }
        }
    }

    /**
     * Resolve with detailed Namecoin error messages.
     */
    private suspend fun resolveWithDetailedErrors(identifier: String): ResolvedIdentifier? {
        // Try Namecoin with detailed errors
        if (NamecoinNameResolver.isNamecoinIdentifier(identifier)) {
            return try {
                val result = namecoinNameService.resolve(identifier)
                ResolvedIdentifier(result.pubkey, namecoinSource = identifier)
            } catch (e: NamecoinLookupException) {
                setState {
                    copy(
                        phase = Phase.Error,
                        errorMessage = importer.namecoinErrorMessage(identifier, e),
                    )
                }
                return null
            }
        }

        return importer.resolveIdentifier(identifier, resolveNip05 = ::resolveNip05)
    }

    /**
     * Resolve a NIP-05 identifier via HTTP.
     */
    private suspend fun resolveNip05(identifier: String): String? = withContext(Dispatchers.IO) {
        try {
            val parts = if (identifier.contains("@")) {
                val split = identifier.split("@", limit = 2)
                split[0] to split[1]
            } else {
                "_" to identifier
            }
            val (name, domain) = parts
            val url = "https://$domain/.well-known/nostr.json?name=$name"
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000
            connection.setRequestProperty("Accept", "application/json")
            val responseText = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val root = json.parseToJsonElement(responseText).jsonObject
            val names = root["names"]?.jsonObject ?: return@withContext null
            val pubkey = names[name]?.jsonPrimitive?.content
            if (pubkey != null && HEX_PUBKEY_REGEX.matches(pubkey)) pubkey else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Fetch a user's follow list via Primal's caching API.
     * Returns parsed FollowEntry list, or null if no contact list found.
     */
    private suspend fun fetchFollowList(pubkeyHex: String): List<FollowEntry>? =
        withContext(Dispatchers.IO) {
            val queryResult = primalApiClient.query(
                message = PrimalCacheFilter(
                    primalVerb = PrimalVerb.CONTACT_LIST.id,
                    optionsJson = FollowListRequestBody(
                        pubkey = pubkeyHex,
                        extendedResponse = false,
                    ).encodeToJsonString(),
                ),
            )

            val followListEvent: NostrEvent =
                queryResult.findNostrEvent(NostrEventKind.FollowList) ?: return@withContext null

            parseFollowEntries(followListEvent)
        }

    /**
     * Parse p-tags from a kind 3 event into FollowEntry list.
     */
    private fun parseFollowEntries(event: NostrEvent): List<FollowEntry> {
        return event.tags
            .filter { tag: JsonArray ->
                tag.size >= 2 && tag[0].jsonPrimitive.content == "p"
            }
            .mapNotNull { tag: JsonArray ->
                val pk = tag[1].jsonPrimitive.content
                if (!HEX_PUBKEY_REGEX.matches(pk)) return@mapNotNull null
                FollowEntry(
                    pubkeyHex = pk.lowercase(),
                    relayHint = tag.getOrNull(2)?.jsonPrimitive?.content?.takeIf { it.isNotBlank() },
                    petname = tag.getOrNull(3)?.jsonPrimitive?.content?.takeIf { it.isNotBlank() },
                )
            }
            .distinctBy { it.pubkeyHex }
    }

    private fun toggleSelection(pubkeyHex: String) {
        setState {
            if (phase != Phase.Preview) return@setState this
            val newSelected = if (pubkeyHex in selected) selected - pubkeyHex else selected + pubkeyHex
            copy(selected = newSelected)
        }
    }

    private fun setSelectAll(selectAll: Boolean) {
        setState {
            if (phase != Phase.Preview) return@setState this
            copy(selected = if (selectAll) follows.map { it.pubkeyHex }.toSet() else emptySet())
        }
    }

    /**
     * Get the currently selected follow entries and transition to Applying state.
     * Returns the selected entries for the caller to apply externally.
     */
    fun getSelectedAndApply(): List<FollowEntry> {
        val current = _state.value
        if (current.phase != Phase.Preview) return emptyList()
        val selected = current.follows.filter { it.pubkeyHex in current.selected }
        setState { copy(phase = Phase.Applying) }
        return selected
    }

    /**
     * Mark the apply as complete.
     */
    fun markDone(count: Int) {
        setState { copy(phase = Phase.Done, appliedCount = count) }
    }

    /**
     * Mark the apply as failed.
     */
    fun markError(message: String) {
        setState { copy(phase = Phase.Error, errorMessage = message) }
    }

    private fun reset() {
        setState {
            UiState()
        }
    }
}
