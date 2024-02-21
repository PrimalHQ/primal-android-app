package net.primal.android.auth.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.ToSAndPrivacyPolicyText
import net.primal.android.core.compose.button.PrimalCallToActionButton
import net.primal.android.core.compose.fadingBottomEdge
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun WelcomeScreen(onSignInClick: () -> Unit, onCreateAccountClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .systemBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .weight(0.6f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.primal_logo),
                    contentDescription = null,
                )

                Image(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(bottom = 40.dp)
                        .fadingBottomEdge()
                        .shadow(
                            elevation = 128.dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = AppTheme.colorScheme.primary,
                            spotColor = AppTheme.colorScheme.primary,
                        ),
                    painter = painterResource(id = R.drawable.welcome),
                    contentDescription = null,
                    alignment = Alignment.BottomCenter,
                )
            }

            BoxWithConstraints(
                modifier = Modifier.weight(0.4f),
                contentAlignment = Alignment.TopCenter,
            ) {
                val maxHeight = with(LocalDensity.current) { maxHeight.roundToPx() }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PrimalCallToActionButton(
                        modifier = Modifier
                            .widthIn(0.dp, 420.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        title = stringResource(id = R.string.welcome_create_account_button_title),
                        subtitle = if (maxHeight > MIN_HEIGHT_FOR_SUBTITLE) {
                            stringResource(id = R.string.welcome_create_account_button_subtitle)
                        } else {
                            null
                        },
                        onClick = onCreateAccountClick,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimalCallToActionButton(
                        modifier = Modifier
                            .widthIn(0.dp, 420.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        title = stringResource(id = R.string.welcome_sign_in_button_title),
                        subtitle = if (maxHeight > MIN_HEIGHT_FOR_SUBTITLE) {
                            stringResource(id = R.string.welcome_sign_in_button_subtitle)
                        } else {
                            null
                        },
                        onClick = onSignInClick,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ToSAndPrivacyPolicyText(
                        modifier = Modifier
                            .widthIn(0.dp, 360.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        tosPrefix = stringResource(id = R.string.welcome_tos_prefix),
                    )
                }
            }
        }
    }
}

private const val MIN_HEIGHT_FOR_SUBTITLE = 500

@Preview
@Composable
fun PreviewWelcomeScreen() {
    PrimalTheme(
        primalTheme = PrimalTheme.Sunset,
    ) {
        WelcomeScreen(
            onSignInClick = {},
            onCreateAccountClick = {},
        )
    }
}
