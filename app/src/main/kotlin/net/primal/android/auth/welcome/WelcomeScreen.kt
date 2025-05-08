package net.primal.android.auth.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.auth.compose.OnboardingButton
import net.primal.android.core.compose.MAX_COMPONENT_WIDTH
import net.primal.android.core.compose.ONE_HALF
import net.primal.android.core.compose.TWO_FIFTHS
import net.primal.android.core.compose.ToSAndPrivacyPolicyText
import net.primal.android.core.compose.UiDensityMode
import net.primal.android.core.compose.detectUiDensityModeFromMaxHeight
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun WelcomeScreen(
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onRedeemCodeClick: () -> Unit,
) {
    ColumnWithBackground(
        backgroundPainter = painterResource(id = R.drawable.onboarding_spot1),
    ) { size ->
        val uiMode = size.height.detectUiDensityModeFromMaxHeight()
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                modifier = Modifier
                    .widthIn(240.dp, MAX_COMPONENT_WIDTH.dp)
                    .heightIn(
                        0.dp,
                        size.height * when (uiMode) {
                            UiDensityMode.Normal, UiDensityMode.Comfortable -> ONE_HALF
                            else -> TWO_FIFTHS
                        },
                    )
                    .fillMaxWidth(),
                painter = painterResource(id = R.drawable.welcome),
                contentDescription = null,
                alignment = Alignment.BottomCenter,
            )

            Image(
                modifier = Modifier.width(200.dp),
                painter = painterResource(id = R.drawable.primal_welcome_logo),
                contentDescription = null,
            )

            Spacer(
                modifier = Modifier.height(
                    when (uiMode) {
                        UiDensityMode.Normal -> 24.dp
                        UiDensityMode.Comfortable -> 20.dp
                        else -> 16.dp
                    },
                ),
            )

            OnboardingButton(
                text = stringResource(id = R.string.welcome_sign_in_button_title),
                onClick = onSignInClick,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OnboardingButton(
                text = stringResource(id = R.string.welcome_create_account_button_title),
                onClick = onCreateAccountClick,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OnboardingButton(
                text = stringResource(id = R.string.welcome_redeem_code_button_title),
                onClick = onRedeemCodeClick,
            )

            ToSAndPrivacyPolicyText(
                modifier = Modifier
                    .widthIn(0.dp, MAX_COMPONENT_WIDTH.dp)
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                color = Color.White,
                fontSize = 16.sp,
                linksColor = Color.White,
                tosPrefix = stringResource(id = R.string.welcome_tos_prefix),
            )
        }
    }
}

@Preview
@Composable
fun PreviewWelcomeScreen() {
    PrimalTheme(
        primalTheme = PrimalTheme.Sunset,
    ) {
        WelcomeScreen(
            onSignInClick = {},
            onCreateAccountClick = {},
            onRedeemCodeClick = {},
        )
    }
}
