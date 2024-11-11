package net.primal.android.premium.manage.nameChange

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.ifNotNull
import net.primal.android.premium.buying.name.PremiumPrimalNameStage
import net.primal.android.premium.manage.nameChange.PremiumChangePrimalNameContract.ChangePrimalNameStage
import net.primal.android.premium.manage.ui.ConfirmNameChangeStage

@Composable
fun PremiumChangePrimalNameScreen(viewModel: PremiumChangePrimalNameViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, viewModel.effects) {
        viewModel.effects.collect {
            when (it) {
                PremiumChangePrimalNameContract.SideEffect.PrimalNameChanged -> onClose()
            }
        }
    }

    PremiumChangePrimalNameScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumChangePrimalNameScreen(
    state: PremiumChangePrimalNameContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (PremiumChangePrimalNameContract.UiEvent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    SnackbarErrorHandler(
        snackbarHostState = snackbarHostState,
        error = state.error,
        errorMessageResolver = {
            when (it) {
                PremiumChangePrimalNameContract.NameChangeError.GenericError ->
                    stringResource(id = R.string.app_generic_error)

                PremiumChangePrimalNameContract.NameChangeError.PrimalNameTaken ->
                    stringResource(id = R.string.premium_name_change_name_taken_error)
            }
        },
        onErrorDismiss = { eventPublisher(PremiumChangePrimalNameContract.UiEvent.DismissError) },
    )

    BackHandler {
        when (state.stage) {
            ChangePrimalNameStage.PickNew -> onClose()
            ChangePrimalNameStage.Confirm -> eventPublisher(
                PremiumChangePrimalNameContract.UiEvent.SetStage(
                    ChangePrimalNameStage.PickNew,
                ),
            )
        }
    }

    AnimatedContent(
        contentKey = { it.name },
        targetState = state.stage,
        transitionSpec = { transitionSpecBetweenStages() },
        label = "ChangePrimalNameStages",
    ) { stage ->

        when (stage) {
            ChangePrimalNameStage.PickNew -> {
                PremiumPrimalNameStage(
                    onBack = onClose,
                    titleText = stringResource(id = R.string.premium_change_primal_name_title),
                    initialName = state.primalName,
                    onPrimalNameAvailable = {
                        eventPublisher(PremiumChangePrimalNameContract.UiEvent.SetPrimalName(it))
                        eventPublisher(PremiumChangePrimalNameContract.UiEvent.SetStage(ChangePrimalNameStage.Confirm))
                    },
                    snackbarHostState = snackbarHostState,
                )
            }

            ChangePrimalNameStage.Confirm -> {
                ChangePremiumNameConfirmationStage(
                    state = state,
                    eventPublisher = eventPublisher,
                    snackbarHostState = snackbarHostState,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ChangePremiumNameConfirmationStage(
    eventPublisher: (PremiumChangePrimalNameContract.UiEvent) -> Unit,
    state: PremiumChangePrimalNameContract.UiState,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_name_changed_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = {
                    eventPublisher(
                        PremiumChangePrimalNameContract.UiEvent.SetStage(ChangePrimalNameStage.PickNew),
                    )
                },
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(all = 36.dp),
                contentAlignment = Alignment.Center,
            ) {
                PrimalLoadingButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.premium_name_changed_button),
                    onClick = {
                        eventPublisher(PremiumChangePrimalNameContract.UiEvent.ConfirmPrimalNameChange)
                    },
                    loading = state.changingName,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        ifNotNull(state.primalName, state.profileDisplayName) { primalName, profileDisplayName ->
            ConfirmNameChangeStage(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxSize(),
                contentPadding = paddingValues,
                primalName = primalName,
                profileAvatarCdnImage = state.profileAvatarCdnImage,
                profileDisplayName = profileDisplayName,
            )
        }
    }
}

private fun AnimatedContentTransitionScope<ChangePrimalNameStage>.transitionSpecBetweenStages() =
    when (initialState) {
        ChangePrimalNameStage.PickNew -> {
            slideInHorizontally(initialOffsetX = { it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
        }

        ChangePrimalNameStage.Confirm -> {
            slideInHorizontally(initialOffsetX = { -it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
        }
    }
