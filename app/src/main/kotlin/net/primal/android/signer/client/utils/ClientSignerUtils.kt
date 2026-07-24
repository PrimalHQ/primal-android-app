package net.primal.android.signer.client.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.net.toUri
import net.primal.android.signer.client.AMBER_PACKAGE_NAME
import net.primal.core.utils.getOrDefault
import net.primal.core.utils.runCatching

/* Amber 3.0.4 */
private const val COMPATIBLE_AMBER_VERSION_CODE = 115

private const val NOSTRSIGNER_SCHEME_URI = "nostrsigner:"

/**
 * A NIP-55 external signer app installed on this device (e.g. Amber, Cambium).
 */
data class ExternalSignerInfo(
    val packageName: String,
    val displayName: String,
)

private fun Context.queryNostrSignerActivities() =
    Intent(Intent.ACTION_VIEW, NOSTRSIGNER_SCHEME_URI.toUri()).let { intent ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
    }

/**
 * Returns all NIP-55 external signer apps installed on this device, resolved by querying for
 * activities handling the `nostrsigner:` scheme (covered by the `<queries>` declaration in the
 * manifest). Primal's own signer activity is excluded, and Amber is included only if the
 * installed version is compatible.
 */
fun getInstalledExternalSigners(context: Context): List<ExternalSignerInfo> {
    val pm = context.packageManager
    return context.queryNostrSignerActivities()
        .mapNotNull { it.activityInfo?.applicationInfo }
        .distinctBy { it.packageName }
        .filterNot { it.packageName == context.packageName }
        .filter { it.packageName != AMBER_PACKAGE_NAME || isCompatibleAmberVersionInstalled(context) }
        .map { appInfo ->
            ExternalSignerInfo(
                packageName = appInfo.packageName,
                displayName = appInfo.loadLabel(pm).toString(),
            )
        }
}

/**
 * Returns the [ComponentName]s of Primal's own `nostrsigner:` activities (its external-signer
 * provider feature). Used to keep Primal out of the system chooser when the `get_public_key`
 * intent is launched unpinned across several installed signers: without this, Primal offers
 * itself as a signer to sign into itself.
 */
fun getOwnSignerComponents(context: Context): List<ComponentName> =
    context.queryNostrSignerActivities()
        .mapNotNull { it.activityInfo }
        .filter { it.packageName == context.packageName }
        .map { ComponentName(it.packageName, it.name) }

fun isCompatibleAmberVersionInstalled(context: Context): Boolean =
    runCatching {
        val pm = context.packageManager
        val packageInfo = pm.getPackageInfo(AMBER_PACKAGE_NAME, 0)
        val installedVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }

        installedVersionCode >= COMPATIBLE_AMBER_VERSION_CODE
    }.getOrDefault(false)
