package net.primal.android.auth.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalCallToActionButton
import net.primal.android.core.compose.fadingBottomEdge
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun WelcomeScreen(
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .systemBarsPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Image(
                painter = painterResource(id = R.drawable.primal_logo),
                contentDescription = null,
            )

            Image(
                modifier = Modifier
                    .padding(bottom = 64.dp)
                    .fadingBottomEdge()
                    .shadow(
                        elevation = 128.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = AppTheme.colorScheme.primary,
                        spotColor = AppTheme.colorScheme.primary,
                    )
                ,
                painter = painterResource(id = R.drawable.welcome),
                contentDescription = null,
            )

            PrimalCallToActionButton(
                modifier = Modifier.padding(horizontal = 32.dp),
                title = stringResource(id = R.string.welcome_create_account_button_title),
                subtitle = stringResource(id = R.string.welcome_create_account_button_subtitle),
                onClick = onCreateAccountClick,
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimalCallToActionButton(
                modifier = Modifier.padding(horizontal = 32.dp),
                title = stringResource(id = R.string.welcome_sign_in_button_title),
                subtitle = stringResource(id = R.string.welcome_sign_in_button_subtitle),
                onClick = onSignInClick,
            )
        }
    }
}


@Preview
@Composable
fun PreviewWelcomeScreen() {
    PrimalTheme(
        theme = PrimalTheme.Sunset
    ) {
        WelcomeScreen(
            onSignInClick = {},
            onCreateAccountClick = {},
        )
    }
}