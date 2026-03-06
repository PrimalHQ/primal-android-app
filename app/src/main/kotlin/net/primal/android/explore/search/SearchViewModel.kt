package net.primal.android.explore.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.core.compose.profile.model.asUserProfileItemUi
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.explore.search.SearchContract.UiEvent
import net.primal.android.explore.search.SearchContract.UiState
import net.primal.android.namecoin.NamecoinNameService
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

    private var namecoinJob: Job? = null

    init {
        observeEvents()
        observeDebouncedQueryChanges()
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
                    resolveNamecoinIfNeeded(query = it.query)
                }
        }

    private fun resolveNamecoinIfNeeded(query: String) {
        // Cancel any in-flight Namecoin lookup before starting a new one
        namecoinJob?.cancel()

        if (!NamecoinNameResolver.isNamecoinIdentifier(query)) {
            setState { copy(namecoinResolvedUser = null, namecoinResolving = false) }
            return
        }

        namecoinJob = viewModelScope.launch {
            setState { copy(namecoinResolving = true, namecoinResolvedUser = null) }
            try {
                val result = namecoinNameService.resolve(query)
                if (result != null) {
                    // Fetch actual Nostr profile for the resolved pubkey
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
                } else {
                    setState { copy(namecoinResolvedUser = null, namecoinResolving = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setState { copy(namecoinResolvedUser = null, namecoinResolving = false) }
            }
        }
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
