package net.primal.android.auth.create

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
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
import net.primal.android.auth.create.CreateContract.UiState.CreateAccountStep
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.button.PrimalOutlinedButton
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
private fun stepColor(step: CreateAccountStep, position: Int): Color {
    return if (position <= step.step) AppTheme.extraColorScheme.onSurfaceVariantAlt1 else AppTheme.colorScheme.outline
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
            if (state.currentStep != CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS) {
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
                .verticalScroll(rememberScrollState())
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

                CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS -> FollowRecommendedAccountsStep(state = state)
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
                loading = state.creatingAccount,
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
fun CreateAccountStep(
    state: CreateContract.UiState, eventPublisher: (CreateContract.UiEvent) -> Unit
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
            val model = ImageRequest.Builder(LocalContext.current).data(state.bannerUri).build()
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
                val model = ImageRequest.Builder(LocalContext.current).data(state.avatarUri).build()

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
    InputField(header = "WEBSITE",
        value = state.website,
        onValueChange = { eventPublisher(CreateContract.UiEvent.WebsiteChangedEvent(it.trim())) })
    Spacer(modifier = Modifier.height(12.dp))
    InputField(header = "ABOUT ME",
        value = state.aboutMe,
        isMultiline = true,
        onValueChange = { eventPublisher(CreateContract.UiEvent.AboutMeChangedEvent(it)) })
    Spacer(modifier = Modifier.height(12.dp))
    InputField(header = "BITCOIN LIGHTNING ADDRESS",
        value = state.lightningAddress,
        onValueChange = { eventPublisher(CreateContract.UiEvent.LightningAddressChangedEvent(it)) })
    Spacer(modifier = Modifier.height(12.dp))
    InputField(header = "NOSTR VERIFICATION (NIP 05)",
        value = state.nip05Identifier,
        onValueChange = { eventPublisher(CreateContract.UiEvent.Nip05IdentifierChangedEvent(it)) })
}

@Composable
fun ProfilePreviewStep(
    state: CreateContract.UiState, isFinalized: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
                    color = if (isFinalized) AppTheme.extraColorScheme.successBright else Color.White,
                    shape = RoundedCornerShape(size = 12.dp)
                ),
        ) {
            if (state.bannerUri != null) {
                val model = ImageRequest.Builder(LocalContext.current).data(state.bannerUri).build()
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
                    .size(size = 78.dp)
                    .clip(shape = CircleShape)
                    .background(color = Color.Black)
                    .align(Alignment.CenterStart)
            ) {
                if (state.avatarUri != null) {
                    val model =
                        ImageRequest.Builder(LocalContext.current).data(state.avatarUri).build()

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
        if (isFinalized) {
            Text(
                text = stringResource(id = R.string.create_success_subtitle),
                fontWeight = FontWeight.W400,
                fontSize = 14.sp,
                lineHeight = 16.41.sp,
                color = AppTheme.extraColorScheme.successBright,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-32).dp) // yuck
            )
        }
        if (isFinalized) {
            Row(
                modifier = Modifier
                    .padding(start = 32.dp, end = 32.dp)
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = Color(0xFF181818), shape = RoundedCornerShape(size = 12.dp)
                    ),
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
        } else {
            Text(
                text = "We will use this info to create \n" + "your Nostr account. If you wish to\n" + "make any changes, you can always \n" + "do so in your profile settings.",
                modifier = Modifier.padding(horizontal = 32.dp),
                fontWeight = FontWeight.W400,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FollowRecommendedAccountsStep(
    state: CreateContract.UiState
) {
    if (state.fetchingRecommendedFollows && state.recommendedFollows.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PrimalLoadingSpinner()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            state.recommendedFollows.forEach {
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF181818), Color(0xFF222222)
                                    )
                                ), shape = RoundedCornerShape(8.dp)
                            )
                            .border(width = 1.dp, color = Color(0xFF222222))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(it.key)
                        PrimalOutlinedButton(
                            onClick = {}
                        ) {
                            Text("Follow All")
                        }
                    }
                }

                items(it.value) { suggestion ->
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 12.dp)
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Color.Black),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val model =
                            ImageRequest.Builder(LocalContext.current).data(suggestion.picture)
                                .build()
                        AsyncImage(
                            model = model,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(48.dp)
                                .width(48.dp)
                                .clip(CircleShape)
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .fillMaxWidth(0.65f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = suggestion.displayName ?: suggestion.name ?: "",
                                fontWeight = FontWeight.W700,
                                fontSize = 14.sp,
                                lineHeight = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White
                            )
                            Text(
                                text = suggestion.nip05 ?: "",
                                fontWeight = FontWeight.W400,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color(0xFF666666)
                            )
                        }
                        PrimalOutlinedButton(
                            onClick = {},
                            modifier = Modifier
                                .defaultMinSize(minWidth = 92.dp)
                                .height(36.dp)
                        ) {
                            Text("Follow")
                        }
                    }
                }
            }
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
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = header,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4
            )
            if (isRequired) {
                Text(buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = Color.Red, fontWeight = FontWeight.W400, fontSize = 16.sp
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
                })
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewCreateScreen(
    @PreviewParameter(CreateScreenPreviewProvider::class) state: CreateScreenPreviewState
) {
    PrimalTheme {
        CreateScreen(state = CreateContract.UiState(
            currentStep = state.currentStep,
            name = state.name,
            handle = state.handle,
            website = state.website,
            aboutMe = state.aboutMe
        ), eventPublisher = {}, onClose = {})
    }
}
