/*
 * Compose UI for Namecoin ElectrumX server settings.
 *
 * Ported from Amethyst PR #1786, adapted to Primal's Material3 patterns.
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.namecoin.NamecoinSettings
import net.primal.android.namecoin.electrumx.ElectrumxClient
import net.primal.android.theme.AppTheme

/**
 * Complete settings section for Namecoin ElectrumX server configuration.
 *
 * Designed to sit in the Network Settings screen alongside existing
 * relay and caching service settings.
 *
 * @param settings        Current [NamecoinSettings] state
 * @param onToggleEnabled Called when user toggles the master switch
 * @param onAddServer     Called with `host:port[:tcp]` when user adds a server
 * @param onRemoveServer  Called with the server string to remove
 * @param onReset         Called when user resets to defaults
 */
@Composable
fun NamecoinSettingsSection(
    settings: NamecoinSettings,
    onToggleEnabled: (Boolean) -> Unit,
    onAddServer: (String) -> Unit,
    onRemoveServer: (String) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // ── Section header with toggle ─────────────────────────────────
        Spacer(modifier = Modifier.height(8.dp))
        TextSection(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            text = "NAMECOIN RESOLUTION",
        )
        PrimalDivider()

        ListItem(
            headlineContent = {
                Text(
                    text = "Enable Namecoin Resolution",
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colorScheme.onPrimary,
                )
            },
            supportingContent = {
                Text(
                    text = "Resolve .bit names via ElectrumX blockchain lookups",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                )
            },
            trailingContent = {
                PrimalSwitch(
                    checked = settings.enabled,
                    onCheckedChange = onToggleEnabled,
                )
            },
            modifier = Modifier.clickable { onToggleEnabled(!settings.enabled) },
            colors = ListItemDefaults.colors(
                containerColor = AppTheme.colorScheme.surfaceVariant,
            ),
        )

        PrimalDivider()

        AnimatedVisibility(
            visible = settings.enabled,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                // ── Active servers display ─────────────────────────────
                ActiveServersDisplay(settings = settings)
                PrimalDivider()

                // ── Custom servers list ────────────────────────────────
                CustomServersList(
                    servers = settings.customServers,
                    onRemove = onRemoveServer,
                )

                // ── Add server input ───────────────────────────────────
                AddServerInput(onAdd = onAddServer)

                // ── Reset button ───────────────────────────────────────
                if (settings.hasCustomServers) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onReset) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Reset to defaults")
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Sub-composables ────────────────────────────────────────────────────

@Composable
private fun TextSection(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = AppTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
private fun ActiveServersDisplay(settings: NamecoinSettings) {
    val servers = settings.toElectrumxServers() ?: ElectrumxClient.DEFAULT_SERVERS
    val isCustom = settings.hasCustomServers

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Active servers",
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colorScheme.onPrimary,
            )
            Text(
                text = if (isCustom) "CUSTOM" else "DEFAULT",
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isCustom) {
                    Color(0xFF4A90D9)
                } else {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt1
                },
                modifier = if (isCustom) {
                    Modifier
                        .background(
                            Color(0xFF4A90D9).copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                } else {
                    Modifier
                },
            )
        }

        Spacer(Modifier.height(8.dp))

        servers.forEach { server ->
            val displayText = "${server.host}:${server.port}" +
                if (!server.useSsl) " (tcp)" else " (tls)"
            ServerRow(displayText = displayText)
        }
    }
}

@Composable
private fun ServerRow(displayText: String) {
    val success = AppTheme.extraColorScheme.successBright
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Green dot (same pattern as NetworkDestinationListItem)
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(8.dp)
                .drawWithCache {
                    onDrawWithContent {
                        drawCircle(color = success)
                    }
                },
        )
        Text(
            text = displayText,
            style = AppTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = AppTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CustomServersList(
    servers: List<String>,
    onRemove: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        if (servers.isEmpty()) {
            Text(
                text = "No custom servers configured",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        } else {
            Text(
                text = "Custom servers (used exclusively)",
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            servers.forEach { server ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = server,
                        style = AppTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { onRemove(server) },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove server",
                            tint = AppTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddServerInput(onAdd: (String) -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    val kb = LocalSoftwareKeyboardController.current

    fun tryAdd() {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            validationError = "Enter a server address"
            return
        }
        val parsed = NamecoinSettings.parseServerString(trimmed)
        if (parsed == null) {
            validationError = "Invalid format. Use host:port or host:port:tcp"
            return
        }
        validationError = null
        onAdd(trimmed)
        input = ""
        kb?.hide()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
                validationError = null
            },
            label = { Text("Add ElectrumX server") },
            placeholder = { Text("host:port or host:port:tcp") },
            singleLine = true,
            isError = validationError != null,
            supportingText = validationError?.let { err ->
                { Text(err, color = AppTheme.colorScheme.error) }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { tryAdd() }),
            textStyle = AppTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
            ),
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = { tryAdd() },
            modifier = Modifier
                .padding(top = 8.dp)
                .size(40.dp),
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add server",
                tint = AppTheme.colorScheme.primary,
            )
        }
    }
}
