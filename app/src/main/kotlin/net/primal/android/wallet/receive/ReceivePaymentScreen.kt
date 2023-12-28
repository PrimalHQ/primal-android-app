package net.primal.android.wallet.receive

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import net.primal.android.R
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Copy
import net.primal.android.crypto.urlToLnUrlHrp
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.api.parseAsLNUrlOrNull
import net.primal.android.wallet.receive.ReceivePaymentContract.UiState
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivePaymentScreen(viewModel: ReceivePaymentViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    ReceivePaymentScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@ExperimentalMaterial3Api
@Composable
fun ReceivePaymentScreen(state: UiState, onClose: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.wallet_receive_transaction_title),
                navigationIcon = PrimalIcons.ArrowBack,
                showDivider = false,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(Color.White, shape = AppTheme.shapes.extraLarge),
                    contentAlignment = Alignment.Center,
                ) {
                    val lnurl = state.lightningAddress?.parseAsLNUrlOrNull()?.urlToLnUrlHrp()
                    if (!lnurl.isNullOrEmpty()) {
                        val drawable = rememberQrCodeDrawable(text = lnurl)
                        Spacer(
                            modifier = Modifier
                                .drawWithContent {
                                    drawIntoCanvas { canvas ->
                                        drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                                        drawable.draw(canvas.nativeCanvas)
                                    }
                                }
                                .fillMaxSize(),
                        )
                    } else {
                        PrimalLoadingSpinner()
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (state.lightningAddress != null) {
                    Text(
                        text = stringResource(id = R.string.wallet_receive_transaction_receiving_to),
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    )

                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = state.lightningAddress,
                        style = AppTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimalLoadingButton(
                        modifier = Modifier.defaultMinSize(minWidth = 160.dp),
                        onClick = {
                            clipboardManager.setText(AnnotatedString(text = state.lightningAddress))
                        },
                        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                        contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        text = stringResource(id = R.string.wallet_receive_transaction_copy_button),
                        leadingIcon = PrimalIcons.Copy,
                    )
                }
            }
        },
    )
}

@Composable
private fun rememberQrCodeDrawable(text: String): Drawable {
    val warningColor = AppTheme.extraColorScheme.warning
    val context = LocalContext.current
    return remember(text) {
        val data = QrData.Text(text)
        Timber.e(data.encode())
        val options = createQrVectorOptions {
            padding = .125f

            logo {
                drawable = context.getDrawable(R.drawable.primal_wave_logo_summer)
                size = .25f
                padding = QrVectorLogoPadding.Natural(.1f)
                shape = QrVectorLogoShape.Circle
            }
            colors {
                ball = QrVectorColor.Solid(warningColor.toArgb())
                frame = QrVectorColor.LinearGradient(
                    colors = listOf(
                        0f to android.graphics.Color.RED,
                        1f to android.graphics.Color.BLUE,
                    ),
                    orientation = QrVectorColor.LinearGradient
                        .Orientation.LeftDiagonal,
                )
            }
            shapes {
                darkPixel = QrVectorPixelShape.RoundCorners(.5f)
                ball = QrVectorBallShape.RoundCorners(.25f)
                frame = QrVectorFrameShape.RoundCorners(.25f)
            }
        }

        QrCodeDrawable(data, options)
    }
}
