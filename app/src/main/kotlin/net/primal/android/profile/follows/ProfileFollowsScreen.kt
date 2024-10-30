package net.primal.android.profile.follows

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.heightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.explore.search.ui.FollowUnfollowVisibility
import net.primal.android.explore.search.ui.UserProfileListItem
import net.primal.android.profile.details.ui.ConfirmFollowUnfollowProfileAlertDialog
import net.primal.android.profile.details.ui.ProfileAction
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.android.profile.follows.ProfileFollowsContract.UiState.FollowsError

@Composable
fun ProfileFollowsScreen(
    viewModel: ProfileFollowsViewModel,
    onProfileClick: (String) -> Unit,
    onClose: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ProfileFollowsScreen(
        state = uiState.value,
        onProfileClick = onProfileClick,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileFollowsScreen(
    state: ProfileFollowsContract.UiState,
    onProfileClick: (String) -> Unit,
    onClose: () -> Unit,
    eventPublisher: (ProfileFollowsContract.UiEvent) -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var lastFollowUnfollowProfileId by rememberSaveable { mutableStateOf<String?>(null) }
    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is FollowsError.FailedToFollowUser -> context.getString(R.string.app_error_unable_to_follow_profile)
                is FollowsError.FailedToUnfollowUser -> context.getString(R.string.app_error_unable_to_unfollow_profile)
                is FollowsError.MissingRelaysConfiguration -> context.getString(R.string.app_missing_relays_config)
            }
        },
        onErrorDismiss = { eventPublisher(ProfileFollowsContract.UiEvent.DismissError) },
    )

    if (state.shouldApproveFollow) {
        ConfirmFollowUnfollowProfileAlertDialog(
            onClose = { eventPublisher(ProfileFollowsContract.UiEvent.DismissConfirmFollowUnfollowAlertDialog) },
            onActionConfirmed = {
                lastFollowUnfollowProfileId?.let {
                    eventPublisher(
                        ProfileFollowsContract.UiEvent.FollowProfile(
                            profileId = it,
                            forceUpdate = true,
                        ),
                    )
                }
            },
            profileAction = ProfileAction.Follow,
        )
    }
    if (state.shouldApproveUnfollow) {
        ConfirmFollowUnfollowProfileAlertDialog(
            onClose = { eventPublisher(ProfileFollowsContract.UiEvent.DismissConfirmFollowUnfollowAlertDialog) },
            onActionConfirmed = {
                lastFollowUnfollowProfileId?.let {
                    eventPublisher(
                        ProfileFollowsContract.UiEvent.UnfollowProfile(
                            profileId = it,
                            forceUpdate = true,
                        ),
                    )
                }
            },
            profileAction = ProfileAction.Unfollow,
        )
    }

    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = state.profileName?.let {
                    when (state.followsType) {
                        ProfileFollowsType.Following -> stringResource(id = R.string.profile_following_title, it)
                        ProfileFollowsType.Followers -> stringResource(id = R.string.profile_followers_title, it)
                    }
                } ?: "",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
            )
        },
        content = { paddingValues ->
            FollowsLazyColumn(
                paddingValues = paddingValues,
                state = state,
                onProfileClick = onProfileClick,
                onFollowProfileClick = {
                    lastFollowUnfollowProfileId = it
                    eventPublisher(
                        ProfileFollowsContract.UiEvent.FollowProfile(
                            profileId = it,
                            forceUpdate = false,
                        ),
                    )
                },
                onUnfollowProfileClick = {
                    lastFollowUnfollowProfileId = it
                    eventPublisher(
                        ProfileFollowsContract.UiEvent.UnfollowProfile(
                            profileId = it,
                            forceUpdate = false,
                        ),
                    )
                },
                onRefreshClick = { eventPublisher(ProfileFollowsContract.UiEvent.ReloadData) },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun FollowsLazyColumn(
    paddingValues: PaddingValues,
    state: ProfileFollowsContract.UiState,
    onProfileClick: (String) -> Unit,
    onFollowProfileClick: (String) -> Unit,
    onUnfollowProfileClick: (String) -> Unit,
    onRefreshClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding(),
    ) {
        items(
            items = state.users,
            key = { it.profileId },
            contentType = { "ProfileListItem" },
        ) { user ->
            UserProfileListItem(
                data = user,
                onClick = { item -> onProfileClick(item.profileId) },
                followUnfollowVisibility = if (user.profileId != state.userId) {
                    FollowUnfollowVisibility.Visible
                } else {
                    FollowUnfollowVisibility.Invisible
                },
                isFollowed = state.userFollowing.contains(user.profileId),
                onFollowUnfollowClick = {
                    if (state.userFollowing.contains(user.profileId)) {
                        onUnfollowProfileClick(user.profileId)
                    } else {
                        onFollowProfileClick(user.profileId)
                    }
                },
            )
            PrimalDivider()
        }

        if (state.users.isEmpty()) {
            when (state.loading) {
                true -> heightAdjustableLoadingLazyListPlaceholder(
                    height = 48.dp,
                    showDivider = true,
                    itemPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                )

                false -> item(contentType = "NoContent") {
                    ListNoContent(
                        modifier = Modifier.fillParentMaxSize(),
                        noContentText = when (state.followsType) {
                            ProfileFollowsType.Following -> stringResource(
                                id = R.string.profile_following_no_content,
                            )

                            ProfileFollowsType.Followers -> stringResource(
                                id = R.string.profile_followers_no_content,
                            )
                        },
                        refreshButtonVisible = true,
                        onRefresh = onRefreshClick,
                    )
                }
            }
        }
    }
}
