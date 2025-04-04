package net.primal.android.auth.onboarding.account.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.auth.compose.OnboardingBottomBar
import net.primal.android.auth.compose.onboardingTextHintTypography
import net.primal.android.auth.onboarding.account.AccountCreationStep
import net.primal.android.auth.onboarding.account.OnboardingContract
import net.primal.android.auth.onboarding.account.OnboardingStep
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.CheckCircleOutline
import net.primal.android.core.compose.icons.primaliconpack.OnboardingZapsExplained
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun OnboardingProfilePreviewScreen(
    state: OnboardingContract.UiState,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
    onBack: () -> Unit,
    onOnboarded: () -> Unit,
    onActivateWallet: () -> Unit,
) {
    val canGoBack = !state.accountCreated && !state.working
    BackHandler(enabled = !canGoBack) {}

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is OnboardingContract.UiState.OnboardingError.CreateAccountFailed ->
                    context.getString(R.string.onboarding_profile_create_error_failed_to_create)

                is OnboardingContract.UiState.OnboardingError.ImageUploadFailed ->
                    context.getString(R.string.onboarding_profile_create_error_image_upload)
            }
        },
        onErrorDismiss = { eventPublisher(OnboardingContract.UiEvent.DismissError) },
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            PrimalTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                title = state.resolveAppBarTitle(),
                textColor = Color.White,
                showDivider = false,
                navigationIcon = if (canGoBack) PrimalIcons.ArrowBack else null,
                navigationIconTintColor = Color.White,
                onNavigationIconClick = { if (canGoBack) onBack() },
            )
        },
        content = { paddingValues ->
            AnimatedContent(
                targetState = state.accountCreationStep,
                label = "OnboardingProfilePreviewContent",
            ) {
                when (it) {
                    AccountCreationStep.AccountPreview -> ProfileAccountPreviewContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        state = state,
                        eventPublisher = eventPublisher,
                    )

                    AccountCreationStep.AccountCreated -> ProfileAccountCreatedContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        state = state,
                    )

                    AccountCreationStep.ZapsIntroduction -> ProfileAccountZapsIntroductionContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    )
                }
            }
        },
        bottomBar = {
            ProfilePreviewBottomBar(
                accountCreationStep = state.accountCreationStep,
                isWorking = state.working,
                onWalletActivationClick = onActivateWallet,
                onFinishOnboardingClick = onOnboarded,
                onContinueClick = { eventPublisher(OnboardingContract.UiEvent.AcknowledgeNostrKeyCreation) },
                onCreateAccountClick = { eventPublisher(OnboardingContract.UiEvent.CreateNostrProfile) },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun ProfilePreviewBottomBar(
    isWorking: Boolean,
    accountCreationStep: AccountCreationStep,
    onCreateAccountClick: () -> Unit,
    onContinueClick: () -> Unit,
    onWalletActivationClick: () -> Unit,
    onFinishOnboardingClick: () -> Unit,
) {
    OnboardingBottomBar(
        buttonText = when (accountCreationStep) {
            AccountCreationStep.AccountPreview -> stringResource(id = R.string.onboarding_button_create_account_now)
            AccountCreationStep.AccountCreated -> stringResource(id = R.string.onboarding_button_continue)
            AccountCreationStep.ZapsIntroduction -> stringResource(id = R.string.onboarding_button_activate_wallet)
        },
        buttonEnabled = !isWorking,
        buttonLoading = isWorking,
        onButtonClick = {
            when (accountCreationStep) {
                AccountCreationStep.AccountPreview -> onCreateAccountClick()
                AccountCreationStep.AccountCreated -> onContinueClick()
                AccountCreationStep.ZapsIntroduction -> onWalletActivationClick()
            }
        },
        footer = {
            if (accountCreationStep == AccountCreationStep.ZapsIntroduction) {
                TextButton(
                    modifier = Modifier.height(56.dp),
                    onClick = onFinishOnboardingClick,
                ) {
                    Text(
                        text = stringResource(id = R.string.onboarding_button_label_i_will_do_this_later),
                        style = onboardingTextHintTypography(),
                    )
                }
            } else {
                OnboardingStepsIndicator(currentPage = OnboardingStep.Preview.index)
            }
        },
    )
}

@Composable
private fun OnboardingContract.UiState.resolveAppBarTitle(): String {
    return when (this.accountCreationStep) {
        AccountCreationStep.AccountPreview -> stringResource(id = R.string.onboarding_title_account_preview)
        AccountCreationStep.AccountCreated -> stringResource(id = R.string.onboarding_title_success)
        AccountCreationStep.ZapsIntroduction -> stringResource(id = R.string.onboarding_title_zaps_introduction)
    }
}

@Composable
private fun ProfileAccountPreviewContent(
    modifier: Modifier = Modifier,
    state: OnboardingContract.UiState,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.weight(weight = 0.5f),
            verticalArrangement = Arrangement.Bottom,
        ) {
            ProfilePreviewBox(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                working = state.working,
                bannerUri = state.bannerUri,
                onBannerUriChanged = {
                    eventPublisher(OnboardingContract.UiEvent.ProfileBannerUriChanged(it))
                },
                avatarUri = state.avatarUri,
                onAvatarUriChanged = {
                    eventPublisher(OnboardingContract.UiEvent.ProfileAvatarUriChanged(it))
                },
                displayName = state.profileDisplayName,
                aboutYou = state.profileAboutYou,
            )
        }

        Column(
            modifier = Modifier.weight(weight = 0.5f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 64.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.onboarding_profile_preview_hint),
                textAlign = TextAlign.Center,
                style = onboardingTextHintTypography(),
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun ProfilePreviewBox(
    modifier: Modifier = Modifier,
    working: Boolean,
    bannerUri: Uri?,
    onBannerUriChanged: (Uri?) -> Unit,
    avatarUri: Uri?,
    onAvatarUriChanged: (Uri?) -> Unit,
    displayName: String,
    aboutYou: String,
) {
    val bannerHeight = 104.dp
    val avatarSize = 78.dp
    val corner = 16.dp
    val shape = RoundedCornerShape(size = corner)
    val bannerShape = RoundedCornerShape(topStart = corner, topEnd = corner, bottomStart = 0.dp, bottomEnd = 0.dp)

    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        onBannerUriChanged(uri)
    }

    fun pickBanner() = bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        onAvatarUriChanged(uri)
    }

    fun pickAvatar() = avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    Box(
        modifier = modifier.background(color = Color.White, shape = shape),
    ) {
        Column {
            BannerBox(
                modifier = Modifier
                    .height(bannerHeight)
                    .clickable(enabled = !working, onClick = { pickBanner() }),
                shape = bannerShape,
                bannerUri = bannerUri,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    modifier = Modifier.clickable(enabled = !working, onClick = { pickBanner() }),
                    text = stringResource(id = R.string.onboarding_profile_preview_change_banner).lowercase(),
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
            ) {
                Text(
                    text = displayName,
                    style = AppTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = aboutYou,
                    style = AppTheme.typography.bodySmall,
                    color = Color.Black,
                )
            }
        }

        PreviewAvatarBox(
            modifier = Modifier
                .padding(start = 16.dp, top = bannerHeight - avatarSize * 2 / 5)
                .size(size = avatarSize)
                .clip(CircleShape)
                .clickable(enabled = !working, onClick = { pickAvatar() }),
            avatarUri = avatarUri,
        )
    }
}

@Composable
private fun PreviewAvatarBox(modifier: Modifier = Modifier, avatarUri: Uri?) {
    Box(
        modifier = modifier.background(color = Color.White, shape = CircleShape),
    ) {
        AnimatedContent(
            targetState = avatarUri,
            label = "ProfilePreviewAvatar",
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        ) {
            when (it) {
                null -> {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = PrimalIcons.AvatarDefault,
                        contentDescription = null,
                    )
                }

                else -> {
                    val model = ImageRequest.Builder(LocalContext.current).data(avatarUri).build()
                    AsyncImage(
                        modifier = Modifier
                            .border(width = 2.dp, color = Color.White, shape = CircleShape)
                            .clip(CircleShape),
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerBox(
    modifier: Modifier = Modifier,
    bannerUri: Uri?,
    shape: RoundedCornerShape,
) {
    Box(
        modifier = modifier.background(color = Color.White, shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        val bannerModifier = Modifier
            .padding(top = 2.dp)
            .padding(horizontal = 2.dp)
            .fillMaxSize()
            .clip(shape)
        val contentScale = ContentScale.FillWidth

        AnimatedContent(
            targetState = bannerUri,
            label = "ProfilePreviewAvatar",
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        ) {
            when (it) {
                null -> {
                    Image(
                        modifier = bannerModifier,
                        painter = painterResource(id = R.drawable.default_banner),
                        contentScale = contentScale,
                        contentDescription = null,
                    )
                }

                else -> {
                    val model = ImageRequest.Builder(LocalContext.current).data(bannerUri).build()
                    AsyncImage(
                        modifier = bannerModifier,
                        model = model,
                        contentDescription = null,
                        contentScale = contentScale,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAccountCreatedContent(modifier: Modifier = Modifier, state: OnboardingContract.UiState) {
    Column(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.weight(weight = 0.6f),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileCreatedSuccessBox(
                modifier = Modifier
                    .padding(horizontal = 32.dp),
                avatarUri = state.avatarUri,
                displayName = state.profileDisplayName,
            )
        }

        Column(
            modifier = Modifier
                .weight(weight = 0.4f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                modifier = Modifier
                    .padding(horizontal = 48.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.onboarding_profile_success_hint),
                textAlign = TextAlign.Center,
                style = onboardingTextHintTypography(),
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun ProfileCreatedSuccessBox(
    modifier: Modifier = Modifier,
    avatarUri: Uri?,
    displayName: String,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SuccessAvatarBox(
            modifier = Modifier.size(size = 108.dp),
            avatarUri = avatarUri,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = displayName,
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            ),
        )

        Spacer(modifier = Modifier.height(36.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.Black.copy(alpha = 0.35f),
                    shape = AppTheme.shapes.large,
                )
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        ) {
            Image(
                imageVector = PrimalIcons.CheckCircleOutline,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = Color.White),
                modifier = Modifier.padding(start = 26.dp, end = 22.dp),
            )
            Text(
                text = stringResource(id = R.string.onboarding_profile_success_description),
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.8f),
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SuccessAvatarBox(modifier: Modifier = Modifier, avatarUri: Uri?) {
    Box(
        modifier = modifier.background(color = Color.White, shape = CircleShape),
    ) {
        AnimatedContent(
            targetState = avatarUri,
            label = "ProfilePreviewAvatar",
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        ) {
            when (it) {
                null -> {
                    Icon(
                        imageVector = PrimalIcons.AvatarDefault,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    val model = ImageRequest.Builder(LocalContext.current).data(avatarUri).build()
                    AsyncImage(
                        modifier = Modifier
                            .border(width = 2.dp, color = Color.White, shape = CircleShape)
                            .clip(CircleShape),
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAccountZapsIntroductionContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                imageVector = PrimalIcons.OnboardingZapsExplained,
                contentDescription = null,
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = stringResource(R.string.onboarding_profile_zaps_introduction_title),
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 36.sp,

            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = stringResource(R.string.onboarding_profile_zaps_introduction_description_1),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 24.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier.padding(horizontal = 40.dp),
                text = stringResource(R.string.onboarding_profile_zaps_introduction_description_2),
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 24.sp,
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewOnboardingProfilePreviewScreen() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        ColumnWithBackground(
            backgroundPainter = painterResource(id = R.drawable.onboarding_spot4),
        ) {
            OnboardingProfilePreviewScreen(
                state = OnboardingContract.UiState(
                    accountCreationStep = AccountCreationStep.AccountPreview,
                    avatarUri = null,
                    bannerUri = null,
                    profileDisplayName = "Preston",
                    profileAboutYou = "Bitcoin & books. My bitcoin can remain in cold storage " +
                        "far longer than the market can remain irrational.",
                ),
                eventPublisher = {},
                onBack = {},
                onOnboarded = {},
                onActivateWallet = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewOnboardingProfileSuccessScreen() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        ColumnWithBackground(
            backgroundPainter = painterResource(id = R.drawable.onboarding_spot4),
        ) {
            OnboardingProfilePreviewScreen(
                state = OnboardingContract.UiState(
                    accountCreated = true,
                    accountCreationStep = AccountCreationStep.AccountCreated,
                    avatarUri = null,
                    bannerUri = null,
                    profileDisplayName = "Preston",
                    profileAboutYou = "Bitcoin & books. My bitcoin can remain in cold storage " +
                        "far longer than the market can remain irrational.",
                ),
                eventPublisher = {},
                onBack = {},
                onOnboarded = {},
                onActivateWallet = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewOnboardingZapsIntroductionScreen() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        ColumnWithBackground(
            backgroundPainter = painterResource(id = R.drawable.onboarding_spot4),
        ) {
            OnboardingProfilePreviewScreen(
                state = OnboardingContract.UiState(
                    accountCreated = true,
                    accountCreationStep = AccountCreationStep.ZapsIntroduction,
                ),
                eventPublisher = {},
                onBack = {},
                onOnboarded = {},
                onActivateWallet = {},
            )
        }
    }
}
