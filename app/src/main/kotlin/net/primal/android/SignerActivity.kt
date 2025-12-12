package net.primal.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import net.primal.android.core.activity.PrimalActivity
import net.primal.android.signer.parser.SignerIntentParser
import net.primal.android.theme.AppTheme

@AndroidEntryPoint
class SignerActivity : PrimalActivity() {
    @Inject
    lateinit var signerIntentParser: SignerIntentParser

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
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
        signerIntentParser.parse(intent, callingPackage)
    }
}
