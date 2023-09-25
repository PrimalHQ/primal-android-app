package net.primal.android.auth.create.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.auth.create.CreateContract
import net.primal.android.auth.create.CreateContract.UiState.CreateAccountStep
import net.primal.android.auth.create.CreateViewModel
import net.primal.android.auth.create.ui.steps.CreateAccountStep
import net.primal.android.auth.create.ui.steps.FollowRecommendedAccountsStep
import net.primal.android.auth.create.ui.steps.ProfilePreviewStep
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun CreateScreen(
    viewModel: CreateViewModel,
    onClose: () -> Unit,
    onCreateSuccess: (String) -> Unit,
) {
    LaunchedEffect(viewModel, onCreateSuccess) {
        viewModel.effect.collect {
            when (it) {
                is CreateContract.SideEffect.AccountCreatedAndPersisted -> onCreateSuccess(it.pubkey)
            }
        }
    }

    LaunchedErrorHandler(viewModel = viewModel)

    val uiState = viewModel.state.collectAsState()
    CreateScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    state: CreateContract.UiState,
    eventPublisher: (CreateContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(topBar = {
        PrimalTopAppBar(
            title = stepTitle(step = state.currentStep),
            navigationIcon = if (state.currentStep == CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS) null else PrimalIcons.ArrowBack,
            onNavigationIconClick = {
                if (state.currentStep == CreateAccountStep.NEW_ACCOUNT) {
                    onClose()
                } else {
                    eventPublisher(CreateContract.UiEvent.GoBack)
                }
            },
        )
    }, content = { paddingValues ->
        CreateContent(
            state = state, eventPublisher = eventPublisher, paddingValues = paddingValues
        )
    })
}

@Composable
fun CreateContent(
    state: CreateContract.UiState,
    eventPublisher: (CreateContract.UiEvent) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(paddingValues = paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.currentStep != CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS) {
            Row(
                modifier = Modifier
                    .height(18.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.clip(shape = RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .background(stepColor(step = state.currentStep, position = 1))
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .background(stepColor(step = state.currentStep, position = 2))
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .background(stepColor(step = state.currentStep, position = 3))
                    )
                }
            }
        }
        val columnModifier =
            if (state.currentStep != CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS) Modifier
                .verticalScroll(
                    rememberScrollState()
                )
                .fillMaxHeight()
                .weight(weight = 1f, fill = true) else Modifier
                .fillMaxHeight()
                .weight(weight = 1f, fill = true)
        Column(
            modifier = columnModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            when (state.currentStep) {
                CreateAccountStep.NEW_ACCOUNT -> CreateAccountStep(
                    state = state, eventPublisher = eventPublisher
                )

                CreateAccountStep.PROFILE_PREVIEW -> ProfilePreviewStep(
                    state = state, isFinalized = false
                )

                CreateAccountStep.ACCOUNT_CREATED -> ProfilePreviewStep(
                    state = state, isFinalized = true
                )

                CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS -> FollowRecommendedAccountsStep(
                    state = state, eventPublisher = eventPublisher
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .padding(horizontal = 32.dp)
        ) {
            PrimalLoadingButton(
                text = stepActionText(state.currentStep),
                enabled = state.name != "" && state.handle != "",
                loading = state.loading,
                onClick = {
                    when (state.currentStep) {
                        CreateAccountStep.NEW_ACCOUNT -> eventPublisher(CreateContract.UiEvent.GoToProfilePreviewStepEvent)
                        CreateAccountStep.PROFILE_PREVIEW -> eventPublisher(CreateContract.UiEvent.GoToNostrCreatedStepEvent)
                        CreateAccountStep.ACCOUNT_CREATED -> eventPublisher(CreateContract.UiEvent.GoToFollowContactsStepEvent)
                        CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS -> eventPublisher(
                            CreateContract.UiEvent.FinishEvent
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
            )
        }
    }
}

@Composable
private fun stepTitle(step: CreateAccountStep): String {
    return when (step) {
        CreateAccountStep.NEW_ACCOUNT -> stringResource(id = R.string.create_title_new_account)
        CreateAccountStep.PROFILE_PREVIEW -> stringResource(id = R.string.create_title_profile_preview)
        CreateAccountStep.ACCOUNT_CREATED -> stringResource(id = R.string.create_title_nostr_account_created)
        CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS -> stringResource(id = R.string.create_title_people_to_follow)
    }
}

@Composable
private fun stepActionText(step: CreateAccountStep): String {
    return when (step) {
        CreateAccountStep.NEW_ACCOUNT -> stringResource(id = R.string.create_action_next)
        CreateAccountStep.PROFILE_PREVIEW -> stringResource(id = R.string.create_action_create_nostr_account)
        CreateAccountStep.ACCOUNT_CREATED -> stringResource(id = R.string.create_action_find_people_to_follow)
        CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS -> stringResource(id = R.string.create_action_finish)
    }
}

@Composable
private fun stepColor(step: CreateAccountStep, position: Int): Color {
    return if (position <= step.step) AppTheme.extraColorScheme.onSurfaceVariantAlt1 else AppTheme.colorScheme.outline
}

@Composable
fun LaunchedErrorHandler(
    viewModel: CreateViewModel
) {
    val genericMessage = stringResource(id = R.string.app_generic_error)
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.state.filter { it.error != null }.map { it.error }.filterNotNull().collect {
            uiScope.launch {
                Toast.makeText(
                    context, genericMessage, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

data class CreateScreenPreviewState(
    val currentStep: CreateAccountStep,
    val name: String = "",
    val handle: String = "",
    val website: String = "",
    val aboutMe: String = "",
    val fetchingRecommendedFollows: Boolean = false,
    val recommendedFollows: Map<String, List<ContentMetadata>> = emptyMap()
)

class CreateScreenPreviewProvider : PreviewParameterProvider<CreateScreenPreviewState> {
    override val values: Sequence<CreateScreenPreviewState>
        get() = sequenceOf(
            CreateScreenPreviewState(currentStep = CreateAccountStep.NEW_ACCOUNT),
            CreateScreenPreviewState(
                currentStep = CreateAccountStep.PROFILE_PREVIEW,
                name = "Preston Pysh",
                handle = "PrestonPysh",
                aboutMe = "Bitcoin & books. My bitcoin can remain in cold storage far longer than the market can remain irrational.",
                website = "https://theinvestorspodcast.com/"
            ),
            CreateScreenPreviewState(
                currentStep = CreateAccountStep.ACCOUNT_CREATED,
                name = "Preston Pysh",
                handle = "PrestonPysh",
                aboutMe = "Bitcoin & books. My bitcoin can remain in cold storage far longer than the market can remain irrational.",
                website = "https://theinvestorspodcast.com/"
            ),
            CreateScreenPreviewState(
                currentStep = CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS,
                name = "Preston Pysh",
                handle = "PrestonPysh",
                aboutMe = "Bitcoin & books. My bitcoin can remain in cold storage far longer than the market can remain irrational.",
                website = "https://theinvestorspodcast.com/",
                fetchingRecommendedFollows = true
            ),
            CreateScreenPreviewState(
                currentStep = CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS,
                name = "Preston Pysh",
                handle = "PrestonPysh",
                aboutMe = "Bitcoin & books. My bitcoin can remain in cold storage far longer than the market can remain irrational.",
                website = "https://theinvestorspodcast.com/",
                fetchingRecommendedFollows = false,
                recommendedFollows = mapOf(
                    "PROMINENT NOSTRICHES" to listOf(
                        ContentMetadata(
                            name = "cameri",
                            displayName = "Camerið\\u009Fª½\\",
                            nip05 = "cameri@elder.nostr.land",
                            picture = "https://nostr.build/i/8cd2fc3d7e6637dc26c6e80b5e1b6ccb4a1e5ba5f2bec67904fe6912a23a85be.jpg",
                            banner = "https://nostr.build/i/nostr.build_90a51a2e50c9f42288260d01b3a2a4a1c7a9df085423abad7809e76429da7cdc.gif",
                            website = "https://primal.net/cameri",
                            about = "@HodlWithLedn. All opinions are my own.\nBitcoiner class of 2021. Core Nostr Developer. Author of Nostream. Ex Relay Operator.",
                            lud16 = "cameri@getalby.com"
                        )
                    )
                )
            )
        )
}

@Preview
@Composable
fun PreviewCreateScreen(
    @PreviewParameter(CreateScreenPreviewProvider::class) state: CreateScreenPreviewState
) {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        CreateScreen(state = CreateContract.UiState(
            currentStep = state.currentStep,
            name = state.name,
            handle = state.handle,
            website = state.website,
            aboutMe = state.aboutMe
        ), eventPublisher = {}, onClose = {})
    }
}
