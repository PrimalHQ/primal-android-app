package net.primal.android.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.acinq.secp256k1.Hex
import net.primal.android.R
import net.primal.android.core.compose.LoadingElevatedButton
import net.primal.android.crypto.Bech32
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun DemoLoginScreen(
    onFeedSelected: (String) -> Unit,
) {

    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .navigationBarsPadding()
                    .padding(paddingValues = paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
            ) {


                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Image(
                        modifier = Modifier.clip(AppTheme.shapes.extraLarge),
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = null,
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                    )
                }


                Text(
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .fillMaxWidth(),
                    text = "Paste npub to see the feed",
                    textAlign = TextAlign.Center
                )

                var npubValue by remember { mutableStateOf("") }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = npubValue,
                    onValueChange = { input -> npubValue = input },
                    singleLine = true,
                    label = { Text(text = "npub") },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )

                LoadingElevatedButton(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.8f)
                        .padding(vertical = 16.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    onClick = {
                        val decoded = Bech32.decodeBytes(npubValue)
                        val hexValue = Hex.encode(decoded.second)
                        onFeedSelected(hexValue)
                    },
                    enabled = npubValue.isNotEmpty(),
                    content = {
                        Text(text = "Go To Feed")
                    }
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "or",
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.labelMedium,
                )

                LoadingElevatedButton(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.8f)
                        .padding(top = 16.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    onClick = {
                        onFeedSelected("9a500dccc084a138330a1d1b2be0d5e86394624325d25084d3eca164e7ea698a")
                    },
                    content = {
                        Text(text = "Go To Nostr Highlights")
                    }
                )

                LoadingElevatedButton(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.8f)
                        .padding(top = 8.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    onClick = {
                        onFeedSelected("global;trending")
                    },
                    content = {
                        Text(text = "Go To Trending 24h")
                    }
                )

                LoadingElevatedButton(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.8f)
                        .padding(top = 8.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    onClick = {
                        onFeedSelected("global;mostzapped4h")
                    },
                    content = {
                        Text(text = "Go To Most-Zapped")
                    }
                )

                LoadingElevatedButton(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.8f)
                        .padding(top = 8.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    onClick = {
                        onFeedSelected("global;latest")
                    },
                    content = {
                        Text(text = "Go To Global Latest")
                    }
                )
            }
        }
    )
}

@Preview
@Composable
fun PreviewLoginScreen() {
    PrimalTheme {
        DemoLoginScreen(
            onFeedSelected = {},
        )
    }
}