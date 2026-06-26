package com.omniclone.engine

import android.content.pm.ApplicationInfo
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Merges a base APK and its split APKs into a single monolithic APK.
 *
 * The merge strategy is:
 * 1. Start with the base APK as the primary source.
 * 2. For each split APK, copy any entries that do not already exist in the base APK.
 *    Split APKs typically contain architecture-specific libraries or resource tables.
 * 3. Preserve the STORED/DEFLATED method of each entry to avoid recompression overhead.
 *
 * No root is required because the input APK paths are obtained via [ApplicationInfo.sourceDir]
 * and [ApplicationInfo.splitSourceDirs], which are readable by the app that requested them.
 */
@Singleton
class SplitApkMerger @Inject constructor() {

    companion object {
        private const val BUFFER_SIZE = 8192
        private val DEX_PATTERN = Regex("classes\\d+\\.dex")
    }

    /**
     * Merge split APKs into a single APK.
     *
     * @param appInfo The [ApplicationInfo] of the installed application.
     * @param outputDir Directory where the merged APK will be written.
     * @return The merged APK file, or the base APK file if no splits exist.
     * @throws IOException if reading or writing fails.
     */
    fun merge(appInfo: ApplicationInfo, outputDir: File): File {
        val baseApk = File(appInfo.sourceDir)
        val splits = appInfo.splitSourceDirs?.map { File(it) }?.filter { it.exists() } ?: emptyList()

        if (splits.isEmpty()) {
            val merged = File(outputDir, "merged.apk")
            baseApk.copyTo(merged, overwrite = true)
            return merged
        }

        val mergedApk = File(outputDir, "merged.apk")
        mergedApk.parentFile?.mkdirs()

        ZipArchiveOutputStream(mergedApk.outputStream().buffered()).use { output ->
            val seenEntries = mutableSetOf<String>()

            // First pass: copy everything from the base APK.
            copyApkEntries(baseApk, output, seenEntries)

            // Second pass: overlay split APK entries that are not already present.
            // Splits must be processed in a deterministic order to make output reproducible.
            splits.sortedBy { it.name }.forEach { split ->
                copySplitEntries(split, output, seenEntries)
            }
        }

        return mergedApk
    }

    /**
     * Merge an explicit list of APK files (base + splits) into a single output file.
     *
     * @param baseApk The base APK file.
     * @param splitApks List of split APK files.
     * @param output The destination merged APK file.
     * @throws IOException if reading or writing fails.
     */
    fun merge(baseApk: File, splitApks: List<File>, output: File) {
        output.parentFile?.mkdirs()

        ZipArchiveOutputStream(output.outputStream().buffered()).use { zos ->
            val seenEntries = mutableSetOf<String>()

            copyApkEntries(baseApk, zos, seenEntries)

            splitApks.filter { it.exists() }.sortedBy { it.name }.forEach { split ->
                copySplitEntries(split, zos, seenEntries)
            }
        }
    }

    private fun copyApkEntries(
        apk: File,
        output: ZipArchiveOutputStream,
        seenEntries: MutableSet<String>
    ) {
        ZipFile(apk).use { zip ->
            zip.entries.asSequence().forEach { entry ->
                if (!seenEntries.add(entry.name)) return@forEach

                val newEntry = ZipArchiveEntry(entry.name)
                newEntry.method = if (entry.method == ZipEntry.STORED) {
                    newEntry.size = entry.size
                    newEntry.compressedSize = entry.compressedSize
                    newEntry.crc = entry.crc
                    ZipEntry.STORED
                } else {
                    ZipEntry.DEFLATED
                }

                output.putArchiveEntry(newEntry)
                zip.getInputStream(entry).use { input ->
                    input.copyTo(output, BUFFER_SIZE)
                }
                output.closeArchiveEntry()
            }
        }
    }

    private fun copySplitEntries(
        split: File,
        output: ZipArchiveOutputStream,
        seenEntries: MutableSet<String>
    ) {
        ZipFile(split).use { zip ->
            zip.entries.asSequence().forEach { entry ->
                // Never overwrite base APK manifest, signature block, or primary DEX files.
                if (entry.name == "AndroidManifest.xml") return@forEach
                if (entry.name.startsWith("META-INF/")) return@forEach
                if (DEX_PATTERN.matches(entry.name)) return@forEach
                if (!seenEntries.add(entry.name)) return@forEach

                val newEntry = ZipArchiveEntry(entry.name)
                newEntry.method = if (entry.method == ZipEntry.STORED) {
                    newEntry.size = entry.size
                    newEntry.compressedSize = entry.compressedSize
                    newEntry.crc = entry.crc
                    ZipEntry.STORED
                } else {
                    ZipEntry.DEFLATED
                }

                output.putArchiveEntry(newEntry)
                zip.getInputStream(entry).use { input ->
                    input.copyTo(output, BUFFER_SIZE)
                }
                output.closeArchiveEntry()
            }
        }
    }

    /**
     * Verify that the merged APK contains a valid AndroidManifest.xml entry.
     */
    fun verifyMergedApk(mergedApk: File): Boolean {
        if (!mergedApk.exists() || mergedApk.length() == 0L) return false
        return try {
            ZipFile(mergedApk).use { zip ->
                zip.getEntry("AndroidManifest.xml") != null
            }
        } catch (_: IOException) {
            false
        }
    }
}
