/*
 * Composable for rendering a Namecoin identifier as a clickable profile link.
 *
 * Resolves the identifier via NamecoinNameService and navigates to the
 * corresponding Nostr profile on success. While resolving or on failure,
 * displays styled text in Namecoin blue (#4A90D9).
 *
 * Ported from Amethyst PR #1915.
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.namecoin.ui

import androidx.compose.foundation.clickable
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import net.primal.android.namecoin.NamecoinNameService

private val NamecoinBlue = Color(0xFF4A90D9)

/**
 * Renders a Namecoin identifier (d/name, id/name, user@domain.bit, domain.bit)
 * as clickable text that resolves to a Nostr profile.
 *
 * @param identifier The raw Namecoin identifier string.
 * @param namecoinNameService Service for resolving identifiers to Nostr pubkeys.
 * @param onProfileClick Callback invoked with the resolved hex pubkey.
 */
@Composable
fun NamecoinIdentifierLink(
    identifier: String,
    namecoinNameService: NamecoinNameService,
    onProfileClick: (profileId: String) -> Unit,
) {
    var resolvedPubkey by remember(identifier) { mutableStateOf<String?>(null) }

    LaunchedEffect(identifier) {
        try {
            val trimmed = identifier.trimEnd('.', ',', '!', '?', ')', ']')
            val result = namecoinNameService.resolve(trimmed)
            resolvedPubkey = result?.pubkey
        } catch (_: Exception) {
            resolvedPubkey = null
        }
    }

    val pubkey = resolvedPubkey
    Text(
        text = identifier,
        color = NamecoinBlue,
        style = LocalTextStyle.current,
        modifier = if (pubkey != null) {
            Modifier.clickable { onProfileClick(pubkey) }
        } else {
            Modifier
        },
    )
}
