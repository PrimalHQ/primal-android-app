package net.primal.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import net.primal.android.core.activity.PrimalActivity
import net.primal.android.signer.SignerContract
import net.primal.android.signer.SignerViewModel
import net.primal.android.signer.utils.toIntent
import net.primal.android.theme.AppTheme
import timber.log.Timber

@AndroidEntryPoint
class SignerActivity : PrimalActivity() {

    private val signerViewModel: SignerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        signerViewModel.processIntent(intent, callingPackage)

        addOnNewIntentListener {
            Timber.tag("LocalSigner").d("Processing intent from listener.")
            signerViewModel.processIntent(it, callingPackage)
        }

        setContent {
            val context = LocalContext.current

            LaunchedEffect(signerViewModel.effects) {
                signerViewModel.effects.collect {
                    when (it) {
                        is SignerContract.SideEffect.RespondToIntent -> {
                            Timber.tag("LocalSigner").d("Responding to intent: ${it.result}")
                            setResult(
                                RESULT_OK,
                                it.result.toIntent().apply {
                                    putExtra("package", context.packageName)
                                },
                            )
                            finish()
                        }
                    }
                }
            }

            ConfigureActivity {
                ModalBottomSheet(
                    contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    onDismissRequest = { finish() },
                ) {
                    Text(
                        modifier = Modifier.height(600.dp),
                        text = "This is a test!",
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.tag("LocalSigner").d("Processing intent.")
        setIntent(intent)
        signerViewModel.processIntent(intent = intent, packageName = callingPackage)
    }
}
