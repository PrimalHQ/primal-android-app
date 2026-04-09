package net.primal.android.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.domain.profile.ProfileRepository

@HiltViewModel
class PrimalDrawerViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val subscriptionsManager: SubscriptionsManager,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        PrimalDrawerContract.UiState(
            menuItems = buildDrawerMenuItems(userId = activeAccountStore.activeUserId()),
        ),
    )

    val state = _state.asStateFlow()
    private fun setState(reducer: PrimalDrawerContract.UiState.() -> PrimalDrawerContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    init {
        observeActiveAccount()
        observeProfile()
        subscribeToBadgesUpdates()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        activeUserAccount = it,
                        menuItems = buildDrawerMenuItems(
                            userId = it.pubkey,
                            hasPremium = it.premiumMembership != null,
                        ),
                        showPremiumBadge = !it.premiumMembership.isPrimalLegendTier() &&
                            it.hasNotSeenPremiumInTheLast(7.days),
                    )
                }
            }
        }

    private fun observeProfile() {
        viewModelScope.launch {
            profileRepository.observeProfileData(profileId = activeAccountStore.activeUserId()).collect {
                setState {
                    copy(
                        legendaryCustomization = it.primalPremiumInfo
                            ?.legendProfile
                            ?.asLegendaryCustomization(),
                    )
                }
            }
        }
    }

    private fun UserAccount.hasNotSeenPremiumInTheLast(duration: Duration): Boolean {
        val lastTimestamp = this.lastBuyPremiumTimestampInMillis ?: 0
        return lastTimestamp < Instant.now().minusSeconds(duration.inWholeSeconds).epochSecond
    }

    private fun subscribeToBadgesUpdates() =
        viewModelScope.launch {
            subscriptionsManager.badges.collect {
                setState {
                    copy(badges = it)
                }
            }
        }

    private fun buildDrawerMenuItems(hasPremium: Boolean = false, userId: String) =
        listOf(
            DrawerScreenDestination.Profile(userId = userId),
            DrawerScreenDestination.Premium(hasPremium = hasPremium),
            DrawerScreenDestination.Messages,
            DrawerScreenDestination.Bookmarks(userId = userId),
            DrawerScreenDestination.RemoteLogin,
            DrawerScreenDestination.ScanCode,
            DrawerScreenDestination.Settings,
            DrawerScreenDestination.SignOut(userId = userId),
        )
}
