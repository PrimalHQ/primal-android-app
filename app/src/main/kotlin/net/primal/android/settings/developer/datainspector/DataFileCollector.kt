package net.primal.android.settings.developer.datainspector

import java.io.File
import net.primal.android.settings.developer.datainspector.DataInspectorContract.DataFile
import net.primal.core.utils.runCatching

fun collectDataFiles(dataRoot: File, excludedDirs: List<File>): List<DataFile> {
    val excludedPaths = excludedDirs.map { it.absolutePath }

    return dataRoot.walkTopDown()
        .filter { it.isFile }
        .filterNot { file -> excludedPaths.any { file.absolutePath.startsWith(it + File.separator) } }
        .mapNotNull { file ->
            runCatching {
                val relativePath = file.relativeTo(dataRoot).path
                DataFile(
                    absolutePath = file.absolutePath,
                    relativePath = relativePath,
                    topLevelFolder = relativePath.substringBefore(
                        delimiter = File.separatorChar,
                        missingDelimiterValue = "",
                    ),
                    sizeBytes = file.length(),
                )
            }.getOrNull()
        }
        .sortedWith(compareBy<DataFile> { it.topLevelFolder }.thenByDescending { it.sizeBytes })
        .toList()
}
