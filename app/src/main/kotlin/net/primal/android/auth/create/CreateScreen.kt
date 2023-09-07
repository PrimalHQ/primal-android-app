package net.primal.android.auth.create

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
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
                is CreateContract.SideEffect.AccountCreated -> onCreateSuccess(it.pubkey)
                else -> null
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

@Composable
private fun stepTitle(step: Int): String {
    return when (step) {
        1 -> stringResource(id = R.string.create_title_new_account)
        2 -> stringResource(id = R.string.create_title_profile_preview)
        3 -> stringResource(id = R.string.create_title_nostr_account_created)
        else -> stringResource(id = R.string.create_title_new_account)
    }
}

@Composable
private fun stepActionText(step: Int): String {
    return when (step) {
        1 -> stringResource(id = R.string.create_action_next)
        2 -> stringResource(id = R.string.create_action_create_nostr_account)
        3 -> stringResource(id = R.string.create_action_finish)
        else -> stringResource(id = R.string.create_action_next)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    state: CreateContract.UiState,
    eventPublisher: (CreateContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stepTitle(step = state.currentStep),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = {
                    if (state.currentStep == 1) {
                        onClose()
                    } else {
                        eventPublisher(CreateContract.UiEvent.GoBack)
                    }
                },
            )
        },
        content = { paddingValues ->
            CreateContent(
                state = state,
                eventPublisher = eventPublisher,
                paddingValues = paddingValues
            )
        }
    )
}

@Composable
private fun stepColor(step: Int, position: Int): Color {
    return if (position <= step) AppTheme.extraColorScheme.onSurfaceVariantAlt1 else AppTheme.colorScheme.outline
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
        Row(
            modifier = Modifier
                .height(18.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(2.dp))
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
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxHeight()
                .weight(weight = 1f, fill = true),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top

        ) {
            when (state.currentStep) {
                1 -> CreateAccountStep(state = state, eventPublisher = eventPublisher)
                2 -> ProfilePreviewStep(state = state)
                3 -> NostrAccountCreatedStep(state = state)
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
                        1 -> eventPublisher(CreateContract.UiEvent.GoToProfilePreviewStepEvent)
                        2 -> eventPublisher(CreateContract.UiEvent.GoToNostrCreatedStepEvent)
                        3 -> eventPublisher(CreateContract.UiEvent.FinishEvent)
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
fun CreateAccountStep(
    state: CreateContract.UiState,
    eventPublisher: (CreateContract.UiEvent) -> Unit
) {
    val avatarPickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            eventPublisher(CreateContract.UiEvent.AvatarUriChangedEvent(avatarUri = uri))
        }
    val bannerPickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            eventPublisher(CreateContract.UiEvent.BannerUriChangedEvent(bannerUri = uri))
        }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .height(179.dp)
    ) {
        if (state.bannerUri != null) {
            val model = ImageRequest.Builder(LocalContext.current)
                .data(state.bannerUri)
                .build()
            AsyncImage(
                model = model,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp)
                    .background(color = Color(0xFF181818))
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(size = 108.dp)
                .clip(shape = CircleShape)
                .background(color = Color.Black)
                .align(Alignment.BottomStart)
        ) {
            if (state.avatarUri != null) {
                val model = ImageRequest.Builder(LocalContext.current)
                    .data(state.avatarUri)
                    .build()

                AsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_avatar),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp)
                .padding(end = 32.dp)
                .height(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = if (state.avatarUri != null) R.string.create_change_avatar else R.string.create_set_avatar),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    avatarPickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppTheme.extraColorScheme.brand1,
                            AppTheme.extraColorScheme.brand2,
                        ),
                    )
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Divider(
                modifier = Modifier
                    .height(16.dp)
                    .width(1.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(id = if (state.bannerUri != null) R.string.create_change_banner else R.string.create_set_banner),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    bannerPickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppTheme.extraColorScheme.brand1,
                            AppTheme.extraColorScheme.brand2,
                        ),
                    )
                )
            )
        }
    }
    Spacer(modifier = Modifier.height(32.dp))
    InputField(
        header = "DISPLAY NAME",
        value = state.name,
        onValueChange = { eventPublisher(CreateContract.UiEvent.NameChangedEvent(it.trim())) },
        isRequired = true
    )
    Spacer(modifier = Modifier.height(12.dp))
    InputField(
        header = "HANDLE",
        value = state.handle,
        onValueChange = { eventPublisher(CreateContract.UiEvent.HandleChangedEvent(it.trim())) },
        isRequired = true,
        prefix = "@"
    )
    Spacer(modifier = Modifier.height(12.dp))
    InputField(
        header = "WEBSITE",
        value = state.website,
        onValueChange = { eventPublisher(CreateContract.UiEvent.WebsiteChangedEvent(it.trim())) })
    Spacer(modifier = Modifier.height(12.dp))
    InputField(
        header = "ABOUT ME",
        value = state.aboutMe,
        isMultiline = true,
        onValueChange = { eventPublisher(CreateContract.UiEvent.AboutMeChangedEvent(it)) })
    Spacer(modifier = Modifier.height(12.dp))
    InputField(
        header = "BITCOIN LIGHTNING ADDRESS",
        value = state.lightningAddress,
        onValueChange = { eventPublisher(CreateContract.UiEvent.LightningAddressChangedEvent(it)) })
    Spacer(modifier = Modifier.height(12.dp))
    InputField(
        header = "NOSTR VERIFICATION (NIP 05)",
        value = state.nip05Identifier,
        onValueChange = { eventPublisher(CreateContract.UiEvent.Nip05IdentifierChangedEvent(it)) })
}

@Composable
fun ProfilePreviewStep(
    state: CreateContract.UiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .height(336.dp)
                .fillMaxWidth()
                .padding(32.dp)
                .clip(RoundedCornerShape(size = 12.dp))
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(size = 12.dp)
                ),
        ) {
            if (state.bannerUri != null) {
                val model = ImageRequest.Builder(LocalContext.current)
                    .data(state.bannerUri)
                    .build()
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(102.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(102.dp)
                        .background(color = Color(0xFF181818))
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 78.dp)
                    .size(size = 78.dp)
                    .clip(shape = CircleShape)
                    .background(color = Color.Black)
                    .align(Alignment.CenterStart)
            ) {
                if (state.avatarUri != null) {
                    val model = ImageRequest.Builder(LocalContext.current)
                        .data(state.avatarUri)
                        .build()

                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_avatar),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
                    .align(alignment = Alignment.BottomCenter),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.name,
                        fontWeight = FontWeight.W700,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        color = AppTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "@${state.handle}",
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4
                    )
                }
                Text(
                    text = state.aboutMe,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = AppTheme.colorScheme.onPrimary
                )
                Text(
                    text = state.website,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = AppTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "We will use this info to create \n" +
                    "your Nostr account. If you wish to\n" +
                    "make any changes, you can always \n" +
                    "do so in your profile settings.",
            modifier = Modifier.padding(horizontal = 32.dp),
            fontWeight = FontWeight.W400,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1
        )
    }
}

@Composable
fun NostrAccountCreatedStep(
    state: CreateContract.UiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .height(336.dp)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(top = 32.dp)
                .clip(RoundedCornerShape(size = 12.dp))
                .border(
                    width = 1.dp,
                    color = AppTheme.extraColorScheme.successBright,
                    shape = RoundedCornerShape(size = 12.dp)
                ),
        ) {
            if (state.bannerUri != null) {
                val model = ImageRequest.Builder(LocalContext.current)
                    .data(state.bannerUri)
                    .build()
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(102.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(102.dp)
                        .background(color = Color(0xFF181818))
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 78.dp)
                    .size(size = 78.dp)
                    .clip(shape = CircleShape)
                    .background(color = Color.Black)
                    .align(Alignment.CenterStart)
            ) {
                if (state.avatarUri != null) {
                    val model = ImageRequest.Builder(LocalContext.current)
                        .data(state.avatarUri)
                        .build()

                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_avatar),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
                    .align(alignment = Alignment.BottomCenter),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.name,
                        fontWeight = FontWeight.W700,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        color = AppTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "@${state.handle}",
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4
                    )
                }
                Text(
                    text = state.aboutMe,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = AppTheme.colorScheme.onPrimary
                )
                Text(
                    text = state.website,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = AppTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = stringResource(id = R.string.create_success_subtitle),
            fontWeight = FontWeight.W400,
            fontSize = 14.sp,
            lineHeight = 16.41.sp,
            color = AppTheme.extraColorScheme.successBright,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier
                .padding(start = 32.dp, end = 32.dp)
                .fillMaxWidth()
                .height(100.dp)
                .background(color = Color(0xFF181818), shape = RoundedCornerShape(size = 12.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(
                painter = painterResource(id = R.drawable.key),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = AppTheme.extraColorScheme.onSurfaceVariantAlt1),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Text(
                text = stringResource(id = R.string.create_finish_subtitle),
                fontWeight = FontWeight.W600,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                color = AppTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(end = 24.dp)
            )
        }
    }
}

@Composable
fun InputField(
    header: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean = false,
    prefix: String? = null,
    isMultiline: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = header,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4
            )
            if (isRequired) {
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color.Red,
                                fontWeight = FontWeight.W400,
                                fontSize = 16.sp
                            )
                        ) {
                            append("*")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                                fontWeight = FontWeight.W400,
                                fontSize = 16.sp
                            )
                        ) {
                            append(" required")
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFF181818),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(size = 12.dp),
            singleLine = !isMultiline,
            minLines = if (isMultiline) 6 else 0,
            value = value,
            onValueChange = onValueChange,
            textStyle = AppTheme.typography.bodySmall,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            leadingIcon = if (prefix != null) {
                {
                    Text(
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                            .padding(bottom = 6.dp), // nothing else worked to center this god damn text vertically
                        text = prefix,
                        fontWeight = FontWeight.W500,
                        fontSize = 18.sp,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4
                    )
                }
            } else null,
        )
    }
}

@Composable
fun LaunchedErrorHandler(
    viewModel: CreateViewModel
) {
    val genericMessage = stringResource(id = R.string.app_generic_error)
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.state
            .filter { it.error != null }
            .map { it.error }
            .filterNotNull()
            .collect {
                uiScope.launch {
                    Toast.makeText(
                        context,
                        genericMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}

data class CreateScreenPreviewState(
    val currentStep: Int,
    val name: String = "",
    val handle: String = "",
    val website: String = "",
    val aboutMe: String = ""
)

class CreateScreenPreviewProvider : PreviewParameterProvider<CreateScreenPreviewState> {
    override val values: Sequence<CreateScreenPreviewState>
        get() = sequenceOf(
            CreateScreenPreviewState(currentStep = 1),
            CreateScreenPreviewState(
                currentStep = 2,
                name = "Preston Pysh",
                handle = "PrestonPysh",
                aboutMe = "Bitcoin & books. My bitcoin can remain in cold storage far longer than the market can remain irrational.",
                website = "https://theinvestorspodcast.com/"
            ),
            CreateScreenPreviewState(
                currentStep = 3, name = "Preston Pysh",
                handle = "PrestonPysh",
                aboutMe = "Bitcoin & books. My bitcoin can remain in cold storage far longer than the market can remain irrational.",
                website = "https://theinvestorspodcast.com/"
            )
        )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewCreateScreen(
    @PreviewParameter(CreateScreenPreviewProvider::class)
    state: CreateScreenPreviewState
) {
    PrimalTheme {
        CreateScreen(
            state = CreateContract.UiState(
                currentStep = state.currentStep,
                name = state.name,
                handle = state.handle,
                website = state.website,
                aboutMe = state.aboutMe
            ),
            eventPublisher = {},
            onClose = {}
        )
    }
}
