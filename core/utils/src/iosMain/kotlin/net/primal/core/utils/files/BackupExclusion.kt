package net.primal.core.utils.files

import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey

/**
 * Marks the file or directory at [path] as excluded from iCloud/Finder backups and device
 * migrations. Excluding a directory covers all files ever created inside it, which is required
 * for files that get recreated at runtime (Room `-wal`/`-shm` sidecars, DataStore atomic
 * writes) since a freshly created file does not inherit a previously set per-file flag.
 */
@OptIn(ExperimentalForeignApi::class)
fun excludeFromBackup(path: String): Boolean =
    memScoped {
        val error = alloc<ObjCObjectVar<NSError?>>()
        val url = NSURL.fileURLWithPath(path)
        val success = url.setResourceValue(
            value = true,
            forKey = NSURLIsExcludedFromBackupKey,
            error = error.ptr,
        )
        if (!success) {
            Napier.w(tag = "BackupExclusion") {
                "Failed to exclude $path from backup: ${error.value?.localizedDescription}"
            }
        }
        success
    }
