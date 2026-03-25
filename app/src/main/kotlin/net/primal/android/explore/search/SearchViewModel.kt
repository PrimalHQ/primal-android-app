package net.primal.android.explore.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.core.compose.profile.model.asUserProfileItemUi
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.explore.search.SearchContract.UiEvent
import net.primal.android.explore.search.SearchContract.UiState
import net.primal.android.namecoin.NamecoinNameService
import net.primal.android.namecoin.electrumx.NamecoinLookupException
import net.primal.android.namecoin.electrumx.NamecoinNameResolver
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.explore.ExploreRepository
import net.primal.domain.profile.ProfileRepository

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val userRepository: UserRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val namecoinNameService: NamecoinNameService,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeDebouncedQueryChanges()
        observeNamecoinResolution()
        observeRecentUsers()
        fetchRecommendedUsers()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SearchQueryUpdated -> setState { copy(searching = true, searchQuery = it.query) }
                    is UiEvent.ProfileSelected -> markProfileInteraction(profileId = it.profileId)
                    UiEvent.ResetSearchQuery -> setState { copy(searchQuery = "", searchResults = emptyList()) }
                }
            }
        }

    @OptIn(FlowPreview::class)
    private fun observeDebouncedQueryChanges() =
        viewModelScope.launch {
            events.filterIsInstance<UiEvent.SearchQueryUpdated>()
                .debounce(0.42.seconds)
                .collect {
                    onSearchQueryChanged(query = it.query)
                }
        }

    /**
     * Uses mapLatest to automatically cancel in-flight Namecoin lookups
     * when a new query arrives, preventing race conditions.
     */
    @OptIn(FlowPreview::class)
    private fun observeNamecoinResolution() =
        viewModelScope.launch {
            events.filterIsInstance<UiEvent.SearchQueryUpdated>()
                .debounce(0.42.seconds)
                .distinctUntilChanged()
                .mapLatest { event ->
                    val query = event.query
                    if (!NamecoinNameResolver.isNamecoinIdentifier(query)) {
                        setState { copy(namecoinResolvedUser = null, namecoinResolving = false, namecoinError = null) }
                        return@mapLatest
                    }

                    setState { copy(namecoinResolving = true, namecoinResolvedUser = null, namecoinError = null) }
                    try {
                        val result = namecoinNameService.resolve(query)
                        val profileData = try {
                            profileRepository.fetchProfile(profileId = result.pubkey)
                        } catch (_: Exception) {
                            null
                        }
                        val namecoinUser = if (profileData != null) {
                            profileData.asUserProfileItemUi().copy(
                                internetIdentifier = profileData.internetIdentifier ?: query,
                            )
                        } else {
                            UserProfileItemUi(
                                profileId = result.pubkey,
                                displayName = query,
                                internetIdentifier = query,
                            )
                        }
                        setState { copy(namecoinResolvedUser = namecoinUser, namecoinResolving = false) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: NamecoinLookupException.NameNotFound) {
                        setState {
                            copy(
                                namecoinResolvedUser = null,
                                namecoinResolving = false,
                                namecoinError = "Name not found on the Namecoin blockchain",
                            )
                        }
                    } catch (e: NamecoinLookupException.NameExpired) {
                        setState {
                            copy(
                                namecoinResolvedUser = null,
                                namecoinResolving = false,
                                namecoinError = "Name expired on the Namecoin blockchain",
                            )
                        }
                    } catch (e: NamecoinLookupException.NoNostrKey) {
                        setState {
                            copy(
                                namecoinResolvedUser = null,
                                namecoinResolving = false,
                                namecoinError = "Name exists but has no Nostr key",
                            )
                        }
                    } catch (e: NamecoinLookupException.ServersUnreachable) {
                        setState {
                            copy(
                                namecoinResolvedUser = null,
                                namecoinResolving = false,
                                namecoinError = "ElectrumX servers unreachable",
                            )
                        }
                    } catch (e: NamecoinLookupException.ParseError) {
                        setState {
                            copy(
                                namecoinResolvedUser = null,
                                namecoinResolving = false,
                                namecoinError = "Invalid response from ElectrumX server",
                            )
                        }
                    } catch (e: Exception) {
                        setState {
                            copy(
                                namecoinResolvedUser = null,
                                namecoinResolving = false,
                                namecoinError = "Resolution failed: ${e.message ?: "unknown error"}",
                            )
                        }
                    }
                }.flowOn(Dispatchers.IO)
                .collect { /* state updates happen inside mapLatest */ }
        }

    private fun onSearchQueryChanged(query: String) =
        viewModelScope.launch {
            setState { copy(searching = true) }
            try {
                val result = exploreRepository.searchUsers(query = query)
                setState { copy(searchResults = result.map { it.mapAsUserProfileUi() }) }
            } catch (error: NetworkException) {
                Napier.w(throwable = error) { "Failed to search users with query: $query" }
            } finally {
                setState { copy(searching = false) }
            }
        }

    private fun observeRecentUsers() {
        viewModelScope.launch {
            userRepository.observeRecentUsers(ownerId = activeAccountStore.activeUserId())
                .distinctUntilChanged()
                .collect {
                    setState { copy(recentUsers = it.map { it.mapAsUserProfileUi() }) }
                }
        }
    }

    private fun fetchRecommendedUsers() =
        viewModelScope.launch {
            try {
                val popularUsers = exploreRepository.fetchPopularUsers()
                setState { copy(popularUsers = popularUsers.map { it.mapAsUserProfileUi() }) }
            } catch (error: NetworkException) {
                Napier.w(throwable = error) { "Failed to fetch recommended users" }
            }
        }

    private fun markProfileInteraction(profileId: String) {
        viewModelScope.launch {
            userRepository.markAsInteracted(profileId = profileId, ownerId = activeAccountStore.activeUserId())
        }
    }
}
