package net.primal.android.auth.create

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
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
                title = "New Account",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
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
                        .background(AppTheme.extraColorScheme.onSurfaceVariantAlt1)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .background(AppTheme.colorScheme.outline)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .background(AppTheme.colorScheme.outline)
                )
            }
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxHeight()
                .weight(weight = 1f, fill = true)

        ) {
            CrateAccountStep(state = state, eventPublisher = eventPublisher)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .padding(horizontal = 32.dp)
        ) {
            PrimalLoadingButton(
                text = "Next",
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
            )
        }
    }
}

@Composable
fun CrateAccountStep(
    state: CreateContract.UiState,
    eventPublisher: (CreateContract.UiEvent) -> Unit
) {
    val avatarPickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                eventPublisher(CreateContract.UiEvent.AvatarUriChangedEvent(avatarUri = uri))
            }
        }
    val bannerPickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                eventPublisher(CreateContract.UiEvent.BannerUriChangedEvent(bannerUri = uri))
            }
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
                text = "set avatar",
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
                text = "set banner",
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
        onValueChange = { eventPublisher(CreateContract.UiEvent.AboutMeChangedEvent(it.trim())) })
}

@Composable
fun ProfilePreviewStep() {

}

@Composable
fun NostrAccountCreatedStep() {

}

@Composable
fun InputField(
    header: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean = false,
    prefix: String? = null
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
            singleLine = true,
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

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewCreateScreen() {
    PrimalTheme {
        CreateScreen(state = CreateContract.UiState(), eventPublisher = {}, onClose = {})
    }
}
