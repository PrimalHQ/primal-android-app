package net.primal.android.settings.developer.datainspector

import io.kotest.matchers.shouldBe
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataFileCollectorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun writeFile(parent: File, relativePath: String, bytes: Int) {
        val file = File(parent, relativePath)
        file.parentFile?.mkdirs()
        file.writeBytes(ByteArray(bytes))
    }

    @Test
    fun `excludes files under excluded dirs`() {
        val root = tempFolder.newFolder("data")
        writeFile(root, "files/keep.txt", 10)
        writeFile(root, "cache/skip.txt", 10)
        val cacheDir = File(root, "cache")

        val result = collectDataFiles(dataRoot = root, excludedDirs = listOf(cacheDir))

        result.map { it.relativePath } shouldBe listOf("files/keep.txt")
    }

    @Test
    fun `sorts by folder ascending then size descending`() {
        val root = tempFolder.newFolder("data")
        writeFile(root, "databases/small.db", 5)
        writeFile(root, "databases/big.db", 50)
        writeFile(root, "files/note.txt", 20)

        val result = collectDataFiles(dataRoot = root, excludedDirs = emptyList())

        result.map { it.relativePath } shouldBe listOf(
            "databases/big.db",
            "databases/small.db",
            "files/note.txt",
        )
    }

    @Test
    fun `derives top level folder and empty string for root files`() {
        val root = tempFolder.newFolder("data")
        writeFile(root, "rootfile.txt", 3)
        writeFile(root, "shared_prefs/p.xml", 3)

        val result = collectDataFiles(dataRoot = root, excludedDirs = emptyList())

        result.first { it.relativePath == "rootfile.txt" }.topLevelFolder shouldBe ""
        result.first { it.relativePath == "shared_prefs/p.xml" }.topLevelFolder shouldBe "shared_prefs"
    }

    @Test
    fun `computes size in bytes`() {
        val root = tempFolder.newFolder("data")
        writeFile(root, "files/data.bin", 42)

        val result = collectDataFiles(dataRoot = root, excludedDirs = emptyList())

        result.single().sizeBytes shouldBe 42L
    }
}
