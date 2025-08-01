package com.example.myapplication1.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.myapplication1.data.Anniversary
import com.example.myapplication1.data.MemoryEvent
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ExportImportUtils {
    
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ ->
            JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE))
        })
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer<LocalDate> { json, _, _ ->
            LocalDate.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE)
        })
        .setPrettyPrinting()
        .create()
    
    data class ExportData(
        val memories: List<MemoryEvent>,
        val anniversaries: List<Anniversary>,
        val exportDate: String = LocalDate.now().toString(),
        val version: String = "1.0"
    )
    
    suspend fun exportMemories(
        context: Context,
        memories: List<MemoryEvent>,
        anniversaries: List<Anniversary>
    ): File? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val timestamp = System.currentTimeMillis()
            val zipFile = File(exportDir, "memories_backup_$timestamp.zip")
            
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                // 导出数据JSON
                val exportData = ExportData(memories, anniversaries)
                val jsonData = gson.toJson(exportData)
                
                val dataEntry = ZipEntry("data.json")
                zipOut.putNextEntry(dataEntry)
                zipOut.write(jsonData.toByteArray())
                zipOut.closeEntry()
                
                // 导出照片文件
                memories.forEach { memory ->
                    memory.photoPaths.forEach { photoPath ->
                        val photoFile = File(photoPath)
                        if (photoFile.exists()) {
                            val photoEntry = ZipEntry("photos/${photoFile.name}")
                            zipOut.putNextEntry(photoEntry)
                            photoFile.inputStream().use { input ->
                                input.copyTo(zipOut)
                            }
                            zipOut.closeEntry()
                        }
                    }
                }
                
                // 导出音频文件
                memories.forEach { memory ->
                    memory.audioPath?.let { audioPath ->
                        val audioFile = File(audioPath)
                        if (audioFile.exists()) {
                            val audioEntry = ZipEntry("audios/${audioFile.name}")
                            zipOut.putNextEntry(audioEntry)
                            audioFile.inputStream().use { input ->
                                input.copyTo(zipOut)
                            }
                            zipOut.closeEntry()
                        }
                    }
                }
            }
            
            zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun importMemories(
        context: Context,
        uri: Uri
    ): ExportData? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext null
            
            var exportData: ExportData? = null
            
            ZipInputStream(inputStream).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    when {
                        entry.name == "data.json" -> {
                            val jsonData = zipIn.readBytes().toString(Charsets.UTF_8)
                            exportData = gson.fromJson(jsonData, ExportData::class.java)
                        }
                        entry.name.startsWith("photos/") -> {
                            val photoDir = FileUtils.getImagesDirectory(context)
                            val photoFile = File(photoDir, entry.name.substringAfter("photos/"))
                            photoFile.parentFile?.mkdirs()
                            
                            FileOutputStream(photoFile).use { outputStream ->
                                val buffer = ByteArray(1024)
                                var length: Int
                                while (zipIn.read(buffer).also { length = it } > 0) {
                                    outputStream.write(buffer, 0, length)
                                }
                            }
                        }
                        entry.name.startsWith("audios/") -> {
                            val audioDir = FileUtils.getAudioDirectory(context)
                            val audioFile = File(audioDir, entry.name.substringAfter("audios/"))
                            audioFile.parentFile?.mkdirs()
                            
                            FileOutputStream(audioFile).use { outputStream ->
                                val buffer = ByteArray(1024)
                                var length: Int
                                while (zipIn.read(buffer).also { length = it } > 0) {
                                    outputStream.write(buffer, 0, length)
                                }
                            }
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
            
            exportData
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importMemories(
        context: Context,
        zipFile: File
    ): ExportData? = withContext(Dispatchers.IO) {
        try {
            var exportData: ExportData? = null
            
            ZipInputStream(zipFile.inputStream()).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    when {
                        entry.name == "data.json" -> {
                            val jsonData = zipIn.readBytes().toString(Charsets.UTF_8)
                            exportData = gson.fromJson(jsonData, ExportData::class.java)
                        }
                        entry.name.startsWith("photos/") -> {
                            val photoDir = FileUtils.getImagesDirectory(context)
                            val photoFile = File(photoDir, entry.name.substringAfter("photos/"))
                            photoFile.parentFile?.mkdirs()
                            
                            val outputStream = FileOutputStream(photoFile)
                            val buffer = ByteArray(1024)
                            var length: Int
                            while (zipIn.read(buffer).also { length = it } > 0) {
                                outputStream.write(buffer, 0, length)
                            }
                            outputStream.close()
                        }
                        entry.name.startsWith("audios/") -> {
                            val audioDir = FileUtils.getAudioDirectory(context)
                            val audioFile = File(audioDir, entry.name.substringAfter("audios/"))
                            audioFile.parentFile?.mkdirs()
                            
                            val outputStream = FileOutputStream(audioFile)
                            val buffer = ByteArray(1024)
                            var length: Int
                            while (zipIn.read(buffer).also { length = it } > 0) {
                                outputStream.write(buffer, 0, length)
                            }
                            outputStream.close()
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
            
            exportData
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun shareBackupFile(context: Context, backupFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "回忆备份文件")
                putExtra(Intent.EXTRA_TEXT, "这是我的回忆备份文件，包含了珍贵的回忆和纪念日。")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "分享回忆备份"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun exportToText(
        context: Context,
        memories: List<MemoryEvent>,
        anniversaries: List<Anniversary>
    ): File? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val timestamp = System.currentTimeMillis()
            val textFile = File(exportDir, "memories_text_$timestamp.txt")
            
            FileWriter(textFile).use { writer ->
                writer.write("=== 我们的回忆 ===\n\n")
                writer.write("导出时间: ${LocalDate.now()}\n\n")
                
                if (anniversaries.isNotEmpty()) {
                    writer.write("=== 纪念日 ===\n")
                    anniversaries.forEach { anniversary ->
                        writer.write("${anniversary.title}\n")
                        writer.write("日期: ${anniversary.date}\n")
                        if (anniversary.description.isNotBlank()) {
                            writer.write("描述: ${anniversary.description}\n")
                        }
                        writer.write("\n")
                    }
                    writer.write("\n")
                }
                
                if (memories.isNotEmpty()) {
                    writer.write("=== 回忆记录 ===\n")
                    memories.sortedBy { it.date }.forEach { memory ->
                        writer.write("${memory.title}\n")
                        writer.write("日期: ${memory.date}\n")
                        writer.write("内容: ${memory.message}\n")
                        if (memory.photoPaths.isNotEmpty()) {
                            writer.write("照片: ${memory.photoPaths.size}张\n")
                        }
                        if (memory.audioPath != null) {
                            writer.write("语音: 有录音\n")
                        }
                        writer.write("---\n\n")
                    }
                }
            }
            
            textFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}