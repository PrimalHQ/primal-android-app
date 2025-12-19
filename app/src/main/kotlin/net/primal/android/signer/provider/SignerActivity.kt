package net.primal.android.signer.provider

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import net.primal.android.core.activity.PrimalActivity
import net.primal.android.signer.model.SignerMethod
import net.primal.android.signer.provider.approvals.PermissionRequestsBottomSheet
import net.primal.android.signer.provider.approvals.PermissionRequestsContract
import net.primal.android.signer.provider.approvals.PermissionRequestsViewModel
import net.primal.android.signer.provider.connect.AndroidConnectScreen
import net.primal.android.signer.provider.connect.AndroidConnectViewModel
import net.primal.android.signer.provider.parser.SignerIntentParser
import net.primal.android.signer.provider.utils.toIntent
import timber.log.Timber

@AndroidEntryPoint
class SignerActivity : PrimalActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConfigureActivity {
                if (intent.isConnectIntent()) {
                    intent.putExtra(CALLING_PACKAGE, callingPackage)
                    val androidConnectViewModel = hiltViewModel<AndroidConnectViewModel>()
                    AndroidConnectScreen(
                        viewModel = androidConnectViewModel,
                        onDismiss = {
                            setResult(RESULT_CANCELED)
                            finish()
                        },
                        onConnectionApproved = { signerMethodResponse ->
                            setResult(
                                RESULT_OK,
                                signerMethodResponse.toIntent().apply {
                                    putExtra("package", packageName)
                                },
                            )
                            finish()
                        },
                    )
                } else {
                    val permissionRequestsViewModel = hiltViewModel<PermissionRequestsViewModel>()
                    permissionRequestsViewModel.processIntent(intent, callingPackage)
                    addOnNewIntentListener {
                        Timber.tag("LocalSigner").d("Processing intent.")
                        permissionRequestsViewModel.processIntent(intent = intent, packageName = callingPackage)
                    }

                    LaunchedEffect(permissionRequestsViewModel.effects) {
                        permissionRequestsViewModel.effects.collect {
                            when (it) {
                                is PermissionRequestsContract.SideEffect.RespondToIntent -> {
                                    Timber.tag("LocalSigner").d("Responding to intent: ${it.result}")
                                    setResult(
                                        RESULT_OK,
                                        it.result.toIntent().apply {
                                            putExtra("package", packageName)
                                        },
                                    )
                                    finish()
                                }
                            }
                        }
                    }

                    PermissionRequestsBottomSheet(
                        viewModel = permissionRequestsViewModel,
                        onDismiss = { finish() },
                    )
                }
            }
        }
    }

    private fun Intent.isConnectIntent(): Boolean {
        return this.getStringExtra(SignerIntentParser.TYPE_COLUMN) == SignerMethod.GET_PUBLIC_KEY.method
    }
}
