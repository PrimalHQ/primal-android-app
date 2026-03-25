/*
 * Diagnostics UI for testing ElectrumX server connectivity.
 *
 * Provides per-server test results with streaming status indicators,
 * human-readable errors, and a diagnostic card with device/TLS info.
 *
 * Ported from Amethyst PR #1937 for Samsung One UI 7 compatibility.
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.namecoin.electrumx.ElectrumxClient
import net.primal.android.namecoin.electrumx.ElectrumxServer
import net.primal.android.namecoin.electrumx.ServerTestResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diagnostics section for testing ElectrumX server connectivity.
 *
 * Shows a "Test Connection" button that tests all configured servers
 * sequentially, streaming results as each test completes. Successfully
 * connected servers have their certs auto-pinned via TOFU.
 *
 * @param servers       List of servers to test
 * @param onTestServer  Suspend function to test a single server
 * @param onPinCert     Called with PEM string to persist a TOFU-pinned cert
 */
@Composable
fun NamecoinDiagnosticsSection(
    servers: List<ElectrumxServer>,
    onTestServer: suspend (ElectrumxServer) -> ServerTestResult,
    onPinCert: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var isTesting by remember { mutableStateOf(false) }
    var testResults by remember { mutableStateOf<List<ServerTestResult>>(emptyList()) }
    var lastTestTimestamp by remember { mutableStateOf<Long?>(null) }

    Column(modifier = modifier) {
        // ── Test button ────────────────────────────────────────
        Button(
            onClick = {
                if (!isTesting) {
                    isTesting = true
                    testResults = emptyList()
                    scope.launch {
                        val results = mutableListOf<ServerTestResult>()
                        for (server in servers) {
                            val result = onTestServer(server)
                            results.add(result)
                            testResults = results.toList()
                            // TOFU: auto-pin cert from successful connections
                            val pem = result.serverCertPem
                            if (result.success && pem != null) {
                                onPinCert(pem)
                            }
                        }
                        lastTestTimestamp = System.currentTimeMillis()
                        isTesting = false
                    }
                }
            },
            enabled = !isTesting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isTesting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.namecoin_testing))
            } else {
                Text(stringResource(R.string.namecoin_test_connection))
            }
        }

        // ── Per-server results ─────────────────────────────────
        if (testResults.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))

            Text(
                stringResource(R.string.namecoin_test_results),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(6.dp))

            testResults.forEach { result ->
                ServerTestResultRow(result)
            }

            if (isTesting && testResults.size < servers.size) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Testing next server…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // ── Diagnostic card ────────────────────────────────────
        if (testResults.isNotEmpty() || lastTestTimestamp != null) {
            Spacer(Modifier.height(16.dp))
            DiagnosticCard(
                testResults = testResults,
                lastTestTimestamp = lastTestTimestamp,
            )
        }
    }
}

@Composable
private fun ServerTestResultRow(result: ServerTestResult) {
    val serverLabel = "${result.server.host}:${result.server.port}"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = if (result.success) "✅" else "❌",
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 6.dp, top = 1.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = serverLabel,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.namecoin_response_time, result.responseTimeMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (result.success) {
                Text(
                    text = stringResource(R.string.namecoin_test_success),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E8B57),
                )
                val fp = result.certFingerprint
                if (fp != null) {
                    Text(
                        text = "Cert: ${fp.take(23)}…",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                val errorText = result.error
                if (errorText != null) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

// ── Diagnostic Card ────────────────────────────────────────────────────

@Composable
private fun DiagnosticCard(
    testResults: List<ServerTestResult>,
    lastTestTimestamp: Long?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                stringResource(R.string.namecoin_diagnostics),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))

            // Last test timestamp
            if (lastTestTimestamp != null) {
                val formatted = remember(lastTestTimestamp) {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        .format(Date(lastTestTimestamp))
                }
                val successCount = testResults.count { it.success }
                val totalCount = testResults.size
                DiagnosticRow(
                    label = stringResource(R.string.namecoin_last_test),
                    value = "$formatted ($successCount/$totalCount OK)",
                )
            } else {
                DiagnosticRow(
                    label = stringResource(R.string.namecoin_last_test),
                    value = stringResource(R.string.namecoin_no_test_yet),
                )
            }

            Spacer(Modifier.height(4.dp))

            // Device info
            DiagnosticRow(
                label = stringResource(R.string.namecoin_device_info),
                value = "${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            )

            Spacer(Modifier.height(4.dp))

            // TLS info from test results
            val tlsVersions = testResults
                .mapNotNull { it.tlsVersion }
                .distinct()
            val tlsDisplay = if (tlsVersions.isNotEmpty()) {
                tlsVersions.joinToString(", ")
            } else {
                "—"
            }
            DiagnosticRow(
                label = stringResource(R.string.namecoin_tls_info),
                value = tlsDisplay,
            )
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(0.65f),
        )
    }
}
