package net.primal.android.auth.onboarding.account.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.primal.android.R
import net.primal.android.auth.compose.DefaultOnboardingAvatar
import net.primal.android.auth.compose.ONE_HALF
import net.primal.android.auth.compose.OnboardingBottomBar
import net.primal.android.auth.compose.defaultOnboardingAvatarBackground
import net.primal.android.auth.compose.onboardingTextHintTypography
import net.primal.android.auth.onboarding.account.OnboardingContract
import net.primal.android.auth.onboarding.account.OnboardingStep
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun OnboardingProfileDetailsScreen(
    state: OnboardingContract.UiState,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = Color.Transparent,
        topBar = {
            PrimalTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                title = stringResource(id = R.string.onboarding_title_create_account),
                textColor = Color.White,
                showDivider = false,
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconTintColor = Color.White,
                onNavigationIconClick = onBack,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp),
            ) {
                ProfileDetailsAvatarColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(ONE_HALF),
                    avatarUri = state.avatarUri,
                    onAvatarUriChanged = {
                        eventPublisher(OnboardingContract.UiEvent.ProfileAvatarUriChanged(it))
                    },
                )
                ProfileDetailsFormColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(ONE_HALF),
                    displayName = state.profileDisplayName,
                    onDisplayNameChanged = {
                        eventPublisher(OnboardingContract.UiEvent.ProfileDisplayNameUpdated(it))
                    },
                    aboutYou = state.profileAboutYou,
                    onAboutYouChanged = {
                        eventPublisher(OnboardingContract.UiEvent.ProfileAboutYouUpdated(it))
                    },
                )
            }
        },
        bottomBar = {
            OnboardingBottomBar(
                buttonText = stringResource(id = R.string.onboarding_button_next),
                buttonEnabled = state.profileDisplayName.isNotEmpty() && state.profileAboutYou.isNotEmpty(),
                onButtonClick = { eventPublisher(OnboardingContract.UiEvent.RequestNextStep) },
                footer = { OnboardingStepsIndicator(currentPage = OnboardingStep.Details.index) },
            )
        },
    )
}

@Composable
private fun ProfileDetailsAvatarColumn(
    modifier: Modifier,
    avatarUri: Uri?,
    onAvatarUriChanged: (Uri?) -> Unit,
) {
    val avatarPickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        onAvatarUriChanged(uri)
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(size = 108.dp)
                .clip(shape = CircleShape)
                .background(color = defaultOnboardingAvatarBackground)
                .clickable {
                    avatarPickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
        ) {
            AnimatedContent(
                targetState = avatarUri,
                label = "AvatarContent",
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            ) {
                when (it) {
                    null -> DefaultOnboardingAvatar()
                    else -> {
                        val model = ImageRequest.Builder(LocalContext.current).data(avatarUri).build()
                        AsyncImage(
                            modifier = Modifier.border(width = 2.dp, color = Color.White, shape = CircleShape),
                            model = model,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        avatarPickMedia.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                ),
            text = if (avatarUri != null) {
                stringResource(id = R.string.onboarding_profile_details_change_photo)
            } else {
                stringResource(id = R.string.onboarding_profile_details_add_photo)
            }.lowercase(),
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

@Composable
private fun ProfileDetailsFormColumn(
    modifier: Modifier,
    displayName: String,
    onDisplayNameChanged: (String) -> Unit,
    aboutYou: String,
    onAboutYouChanged: (String) -> Unit,
) {
    val keyboardVisible by keyboardVisibilityAsState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnboardingOutlinedTextField(
            value = displayName,
            onValueChange = onDisplayNameChanged,
            placeholderText = stringResource(id = R.string.onboarding_profile_details_display_name_hint),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(modifier = Modifier.height(24.dp))

        OnboardingOutlinedTextField(
            value = aboutYou,
            onValueChange = onAboutYouChanged,
            placeholderText = stringResource(id = R.string.onboarding_profile_details_about_you_hint),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!keyboardVisible) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                text = stringResource(id = R.string.onboarding_profile_details_hint),
                textAlign = TextAlign.Center,
                style = onboardingTextHintTypography(),
            )
        }
    }
}

@Composable
fun OnboardingOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        shape = AppTheme.shapes.extraLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            focusedBorderColor = Color.White,
            focusedTextColor = Color.Black,
            unfocusedContainerColor = Color.White,
            unfocusedBorderColor = Color.White,
            unfocusedTextColor = Color.Black,
            disabledContainerColor = Color.White,
            disabledBorderColor = Color.White,
            errorContainerColor = Color.White,
            errorBorderColor = AppTheme.colorScheme.error,
        ),
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = placeholderText,
                style = AppTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = AppTheme.typography.bodyLarge.copy(
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        singleLine = true,
    )
}
